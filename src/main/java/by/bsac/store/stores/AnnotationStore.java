package by.bsac.store.stores;

/**
 * Store that's hold java classes annotated with custom annotations.
 */
public interface AnnotationStore {

    /**
     * Every store class name must have this prefix.
     */
    String ANNOTATION_SUFFIX = "AnnotationStore";

    /**
     * Implements this method to get array of classes annotated with custom annotation.
     * @return - array of annotated {@link Class}.
     */
    Class[] annotatedClasses();
}
