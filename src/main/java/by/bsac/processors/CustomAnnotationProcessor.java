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
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes({"by.bsac.annotations.Custom", "by.bsac.annotations.EnableCustomAnnotations"})
public class CustomAnnotationProcessor extends AbstractProcessor {

    //Fields
    private Filer filer;
    private Messager messager;
    private Elements elements_utils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.elements_utils = processingEnv.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        //Get class annotated with @EnableCustomAnnotations
        Set<? extends Element> enable_annotated_elements = roundEnv.getElementsAnnotatedWith(EnableCustomAnnotations.class);
        Set<TypeElement> enable_annotated_classes = new HashSet<>();
        for (Element enable_annotated: enable_annotated_elements)
            if(enable_annotated.getKind() == ElementKind.CLASS) enable_annotated_classes.add((TypeElement) enable_annotated_classes);

        //Get annotations annotated with @Custom annotation
        Set<? extends Element> annotated_elements = roundEnv.getElementsAnnotatedWith(Custom.class);
        Set<TypeElement> annotated_annotations = new HashSet<>();
        for (Element element : annotated_elements) {
            if (element.getKind() == ElementKind.ANNOTATION_TYPE) annotated_annotations.add((TypeElement) element);
        }

        //Iterate all custom annotations (annotated annotations)
        for (TypeElement custom_annotation : annotated_annotations) {

            Set<? extends Element> elements_annotated_with_custom_annotation = roundEnv.getElementsAnnotatedWith(custom_annotation);
            Set<Class> annotated_classes = new HashSet<>();
            //Determine type of annotated elements
            for (Element element : elements_annotated_with_custom_annotation) {
                if (element.getKind() == ElementKind.CLASS) annotated_classes.add(element.getClass());
            }

            //Generate java files
            StoreBuilder store_obj = new StoreBuilder.Builder()
                    .withAnnotationName(custom_annotation.getClass().getSimpleName())
                    .withAnnotatedClasses(annotated_classes)
                    .build();

            JavaFile file = store_obj.sourceFile("by.bsac.store.stores");

            //Write files
            try {
                file.writeTo(this.filer);
            } catch (IOException e) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                e.printStackTrace();
            }
        }





        return true;
    }

}
