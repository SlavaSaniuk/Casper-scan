package by.bsac.processors;

import by.bsac.annotations.Custom;
import by.bsac.annotations.EnableCustomAnnotations;
import by.bsac.core.StoreBuilder;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes({"by.bsac.annotations.Custom", "by.bsac.annotations.EnableCustomAnnotations"})
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

        //Get class annotated with @EnableCustomAnnotations
        Set<? extends Element> enable_annotated_elements = roundEnv.getElementsAnnotatedWith(EnableCustomAnnotations.class);

        //Get annotated classes (TypeElement)
        Set<TypeElement> enable_annotated_types = new HashSet<>();
        for (Element element : enable_annotated_elements)
            if (element.getKind() == ElementKind.CLASS) enable_annotated_types.add((TypeElement) element);

         //Check whether how many classes annotated with @EnableCustomAnnotation
        if (enable_annotated_types.size() <= 0) {
            this.messager.printMessage(Diagnostic.Kind.WARNING, "No classes annotated with [@" +EnableCustomAnnotations.class.getSimpleName() +"]. Skip custom annotation scanning.");
            return true;
        }

        if (enable_annotated_types.size() > 1)
            this.messager.printMessage(Diagnostic.Kind.WARNING, "More than one class annotated with [@" +EnableCustomAnnotations.class.getSimpleName() +"]. Select random package name value");

        //Get package name value
        final String PACKAGE_NAME = "by.bsac.store.stores"; //default value

        /*
        for (TypeElement element : enable_annotated_types) {
            EnableCustomAnnotations enable = element.getAnnotation(EnableCustomAnnotations.class);
            if (!enable.value().isEmpty()) {
                PACKAGE_NAME = enable.value();
                break;
            }
        }
         */

        //Print package name
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

             Set<String> annotated_classes_names = new HashSet<>();
             for (TypeElement element : types_annotated_with_custom_annotation)
                 annotated_classes_names.add(element.getQualifiedName().toString());

             //Generate source file
            StoreBuilder sourceGen = new StoreBuilder.Builder()
                    .withAnnotationName(custom_annotation.getSimpleName().toString()) //Simple name of custom annotation
                    .withAnnotatedClassesAsStrings(annotated_classes_names)
                    .build();


            //Write source file
            try {
                sourceGen.sourceFile(PACKAGE_NAME).writeTo(this.filer);
            } catch (IOException e) {
                e.printStackTrace();
                this.messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }
        return true;
    }

}
