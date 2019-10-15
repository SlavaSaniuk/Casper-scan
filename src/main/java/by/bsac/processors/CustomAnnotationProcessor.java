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
import javax.lang.model.type.TypeMirror;
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

        //Get classes annotated with @EnableCustomAnnotation
        Set<? extends Element> enable_annotated_elements = roundEnv.getElementsAnnotatedWith(EnableCustomAnnotations.class);
        Set<Class> enable_annotated_classes = new HashSet<>();
        for (Element element : enable_annotated_elements) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement type_elem = (TypeElement) element;
                type_elem.getAnnotation(sd)
                try {

                    Class annotated_class = Class.forName(this.elements_utils.getBinaryName(type_elem).toString());
                    enable_annotated_classes.add(annotated_class);
                } catch (ClassNotFoundException e) {
                    this.messager.printMessage(Diagnostic.Kind.WARNING, "No class def found with name: " +type_elem.getQualifiedName().toString());
                    this.messager.printMessage(Diagnostic.Kind.NOTE, "Skip this TypeElement");
                }
            }
        }

        //Check whether how many classes annotated with @EnableCustomAnnotation
        //If <= 0 - disable custom annotation scanning
        if (enable_annotated_classes.size() <= 0) {
            this.messager.printMessage(Diagnostic.Kind.WARNING, "No classes annotated with @" + EnableCustomAnnotations.class.getSimpleName()
                    +".@" +Custom.class.getSimpleName() + " annotation scanning will not work.");
            return true;
        }
        //If > 1 - select random package name value
        if (enable_annotated_classes.size() > 1) {
            this.messager.printMessage(Diagnostic.Kind.WARNING, "Two or more classes annotated with @" +EnableCustomAnnotations.class.getSimpleName()
                    +". Select random package name value");
        }

        //Get path
        String PACKAGE_NAME = null;
        for (Class clazz : enable_annotated_classes) {
            EnableCustomAnnotations enable_annotation = (EnableCustomAnnotations) clazz.getAnnotation(clazz);

            if (enable_annotation.value() != null && !enable_annotation.value().isEmpty()) {
                PACKAGE_NAME = enable_annotation.value();
                break;
            }
        }

        //Print package name where will be generated new source files (annotations stores)
        //Check whether package name is not null
        if (PACKAGE_NAME == null) {
            this.messager.printMessage(Diagnostic.Kind.WARNING, "Package name is not specified. Disable annotation scanning");
            return true;
        }

        this.messager.printMessage(Diagnostic.Kind.NOTE, "Package name where will be generated new annotations stores [" +PACKAGE_NAME +"];");

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
