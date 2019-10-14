package by.bsac.processors;

import by.bsac.annotations.Custom;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class CustomAnnotationProcessor extends AbstractProcessor {

    //Fields
    private Filer filer;
    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supported_annotations = new HashSet<>();
        supported_annotations.add(Custom.class.getCanonicalName());
        return supported_annotations;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        //Get annotations annotated with @Custom annotation
        Set<? extends Element> annotated_elements = roundEnv.getElementsAnnotatedWith(Custom.class);
        Set<TypeElement> annotated = new HashSet<>();
        for (Element element : annotated_elements) {
            if (element.getKind() == ElementKind.ANNOTATION_TYPE) annotated.add((TypeElement) element);
        }


        try{
            FileObject fo = this.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "File.txt");
            this.messager.printMessage(Diagnostic.Kind.NOTE, fo.toUri().getPath());
            Writer w = fo.openWriter();
            w.write("Hello world!");
            w.flush();
            w.close();
        } catch (FilerException e) {
            return true;
        } catch (IOException e) {
            this.messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

        return true;
    }

}
