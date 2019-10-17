package by.bsac.processors;

import by.bsac.annotations.Custom;
import by.bsac.core.StoreBuilder;
import by.bsac.store.CustomAnnotationStore;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"by.bsac.annotations.Custom"})
public class CustomAnnotationProcessor extends AbstractProcessor {

    //Fields
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        //package name value

        final String PACKAGE_NAME = CustomAnnotationStore.ANNOTATION_STORES_PACKAGE_NAME; //default value
        this.messager.printMessage(Diagnostic.Kind.NOTE, "Package name of annotations stores storage: " +PACKAGE_NAME);

        //Get annotations elements annotated with @Custom annotation
        Set<? extends Element> annotated_elements = roundEnv.getElementsAnnotatedWith(Custom.class);
        Set<TypeElement> custom_annotated_types = new HashSet<>();
        for (Element element : annotated_elements)
            if (element.getKind() == ElementKind.ANNOTATION_TYPE) custom_annotated_types.add((TypeElement) element);

        //Print number of customs annotations
        this.messager.printMessage(Diagnostic.Kind.NOTE, "Found [" +custom_annotated_types.size() +"] custom annotations. Try to process it.");

        //Process customs annotations
        for (TypeElement custom_annotation : custom_annotated_types) {

            //Custom annotation canonical name
            this.messager.printMessage(Diagnostic.Kind.NOTE, "Start to process [" +custom_annotation.getQualifiedName().toString() +"] custom annotation");

            //Get ElementTypes annotated with custom annotations
            Set<? extends Element> elements_annotated_with_custom_annotation = roundEnv.getElementsAnnotatedWith(custom_annotation);
            Set<TypeElement> types_annotated_with_custom_annotation = new HashSet<>();
            for (Element element : elements_annotated_with_custom_annotation)
                if (element.getKind() == ElementKind.CLASS) types_annotated_with_custom_annotation.add((TypeElement) element);
            this.messager.printMessage(Diagnostic.Kind.NOTE, "Project has a " +types_annotated_with_custom_annotation.size()
                    +" classes annotated with [" +custom_annotation.getQualifiedName() +"].");

             Set<String> annotated_classes_names = new HashSet<>();
             for (TypeElement element : types_annotated_with_custom_annotation)
                 annotated_classes_names.add(element.getQualifiedName().toString());

             //Generate source file
            StoreBuilder sourceGen = new StoreBuilder.Builder()
                    .withAnnotationName(custom_annotation.getSimpleName().toString()) //Simple name of custom annotation
                    .withAnnotatedClassesAsStrings(annotated_classes_names)
                    .build();

            //Write source file
            JavaFile source_file = sourceGen.sourceFile(PACKAGE_NAME);

            try {
                source_file.writeTo(this.filer);
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
        }

        return true;
    }

}
