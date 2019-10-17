package by.bsac.core.exceptions;

import java.lang.annotation.Annotation;

public class CustomAnnotationNotRegisteredException extends CasperException {

    public CustomAnnotationNotRegisteredException(String msg) {
        super(msg);
    }

    public CustomAnnotationNotRegisteredException(Class<? extends Annotation> annotation) {
        super("AnnotationStore for annotation [" +annotation.getCanonicalName() +"] not found. Maybe you not annotated your custom annotation with @Custom annotation.");
    }
}
