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

import java.io.IOException;import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"by.bsac.annotations.Custom"})
public class CustomAnnotationProcessor extends AbstractProcessor {

    //Fields
    private Filer filer;
    private Messager messager;
    private final String PACKAGE_NAME = CustomAnnotationStore.ANNOTATION_STORES_PACKAGE_NAME;
    private final List<JavaFile> java_sources = new ArrayList<>();


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
        this.messager.printMessage(Diagnostic.Kind.NOTE, "Package name of annotations stores storage: " +PACKAGE_NAME);

        //Get annotations elements annotated with @Custom annotation
        Set<? extends Element> annotated_elements = roundEnv.getElementsAnnotatedWith(Custom.class);
        Set<TypeElement> custom_annotated_types = new HashSet<>();
        for (Element element : annotated_elements)
            if (element.getKind() == ElementKind.ANNOTATION_TYPE) custom_annotated_types.add((TypeElement) element);

        //Print number of customs annotations
        this.messager.printMessage(Diagnostic.Kind.NOTE, "Found [" +custom_annotated_types.size() +"] custom annotations. Try to process it.");

        //Process customs annotations
        //And generate annotation stores
        for (TypeElement custom_annotation : custom_annotated_types) {

            //Custom annotation canonical name
            this.messager.printMessage(Diagnostic.Kind.NOTE, "Start to process [" + custom_annotation.getQualifiedName().toString() + "] custom annotation");

            //Get ElementTypes annotated with custom annotations
            Set<? extends Element> elements_annotated_with_custom_annotation = roundEnv.getElementsAnnotatedWith(custom_annotation);
            Set<TypeElement> types_annotated_with_custom_annotation = new HashSet<>();
            for (Element element : elements_annotated_with_custom_annotation)
                if (element.getKind() == ElementKind.CLASS)
                    types_annotated_with_custom_annotation.add((TypeElement) element);
            this.messager.printMessage(Diagnostic.Kind.NOTE, "Project has a " + types_annotated_with_custom_annotation.size()
                    + " classes annotated with [" + custom_annotation.getQualifiedName() + "].");

            Set<String> annotated_classes_names = new HashSet<>();
            for (TypeElement element : types_annotated_with_custom_annotation)
                annotated_classes_names.add(element.getQualifiedName().toString());

            //Generate source file
            StoreBuilder sourceGen = new StoreBuilder.Builder()
                    .withAnnotationName(custom_annotation.getSimpleName().toString()) //Simple name of custom annotation
                    .withAnnotatedClassesAsStrings(annotated_classes_names)
                    .build();

            //Add to sources list
            this.java_sources.add(sourceGen.sourceFile(PACKAGE_NAME));
        }

        //Check if processing over
        if (roundEnv.processingOver()) {
            //Write generated sources
            writeSources(this.java_sources);
        }

        return true;
    }

    private void writeSources(List<JavaFile> sources) {

        this.messager.printMessage(Diagnostic.Kind.NOTE, "Start to write generated [" +sources.size() +"] source files.");

        //Iterate about all generated source files
        for (JavaFile source : sources) {

            String canonical_name = PACKAGE_NAME + source.typeSpec.name;

            //Check if source files already written
            try {
                //Get old source file
                FileObject old_source = this.filer.getResource(StandardLocation.SOURCE_OUTPUT, PACKAGE_NAME,
                        source.typeSpec.name + JavaFileObject.Kind.SOURCE.extension);


                //Check if sources files are equals
                //If equals - skip this file
                if (source.toString().contentEquals(old_source.getCharContent(true))) {
                    this.messager.printMessage(Diagnostic.Kind.OTHER, "Content for [" +canonical_name +"] of old and new sources are equals. Skip new source file.");
                    continue;
                }


                //Else - write new source file
                this.messager.printMessage(Diagnostic.Kind.OTHER, "Content for [" +canonical_name +"] of old and new sources are NOT equals. Try to write new source file.");

                boolean deleted = old_source.delete();
                this.messager.printMessage(Diagnostic.Kind.OTHER, "Old source file is deleted: " +deleted);

                try {
                    source.writeTo(this.filer);
                }catch (IOException e) {
                    this.messager.printMessage(Diagnostic.Kind.WARNING, "IOException occurs when try to write new source file.");
                    this.messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                }


                //If getResource() throws IO Exception
            } catch (IOException e) {
                this.messager.printMessage(Diagnostic.Kind.NOTE, "Old source file [" +PACKAGE_NAME +source.typeSpec.name +"] cannot be opening.");
                this.messager.printMessage(Diagnostic.Kind.NOTE, e.getMessage());

                //Try to write new source
                try {
                    source.writeTo(this.filer);
                } catch (IOException ex) {
                    this.messager.printMessage(Diagnostic.Kind.ERROR, "IOException occurs when try to write new source file.");
                    this.messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                }
            }
        }
    }



}
