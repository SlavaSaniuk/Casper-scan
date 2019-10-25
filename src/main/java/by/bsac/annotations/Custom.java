package by.bsac.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which mark custom annotation class to be processed
 * by {@link by.bsac.processors.CustomAnnotationProcessor} class.
 * All custom user defined annotations must be marked with this annotation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Custom {
}
