package by.bsac.processors;

import by.bsac.annotations.Custom;
import by.bsac.core.StoreBuilder;
import by.bsac.store.CustomAnnotationStore;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.*;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashSet;
import java.util.Set;

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

             Set<String> annotated_classes_names = new HashSet<>();
             for (TypeElement element : types_annotated_with_custom_annotation)
                 annotated_classes_names.add(element.getQualifiedName().toString());

             //Generate source file
            StoreBuilder sourceGen = new StoreBuilder.Builder()
                    .withAnnotationName(custom_annotation.getSimpleName().toString()) //Simple name of custom annotation
                    .withAnnotatedClassesAsStrings(annotated_classes_names)
                    .build();

            JavaFile source_file = sourceGen.sourceFile(PACKAGE_NAME);
            //Write source file
            try {

               String package_name_path = PACKAGE_NAME.replace('.','/');
               FileObject old_source = this.filer.getResource(StandardLocation.SOURCE_PATH, PACKAGE_NAME, source_file.typeSpec.name + JavaFileObject.Kind.SOURCE.extension);


               if (old_source.getCharContent(true).equals(source_file.toString()))  {
                   this.messager.printMessage(Diagnostic.Kind.NOTE, "Old and current source files has a same content");
                   return true;
               }else {
                   this.messager.printMessage(Diagnostic.Kind.NOTE, "In new source file content was updated");
                   source_file.writeTo(this.filer);
               }


            }catch (FileNotFoundException e) {
                this.messager.printMessage(Diagnostic.Kind.NOTE, "Write new source file");
                try {
                    source_file.writeTo(this.filer);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return true;
                }
            }catch (IOException e) {
                e.printStackTrace();
                this.messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                return true;
            }
        }

        return true;
    }

}
