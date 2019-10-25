package by.bsac.store;

import by.bsac.core.exceptions.CasperException;
import by.bsac.store.stores.AnnotationStore;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class that suggest simple access to custom annotations in runtime.
 */
public class CustomAnnotationStore {

    private static final Map<Class<? extends Annotation>, AnnotationStore> INITIALIZED_ANNOTATION_STORES = new HashMap<>();
    public static final String ANNOTATION_STORES_PACKAGE_NAME = "by.bsac.store.stores";

    /**
     * Return all classes annotated with custom annotation.
     * @param a_annotation - {@link Class} custom annotation.
     * @return - Array of annotated {@link Class}.
     */
    public static Class[] getClassesAnnotatedWith(Class<? extends Annotation> a_annotation) {

        //Check annotation parameter
        if (a_annotation == null) throw new NullPointerException("Annotation method parameter is null.");
        if (!a_annotation.isAnnotation()) throw new IllegalArgumentException("Annotation method parameter is not a annotation.");

        //Check if annotation store already initialized
        if (INITIALIZED_ANNOTATION_STORES.containsKey(a_annotation))
            return INITIALIZED_ANNOTATION_STORES.get(a_annotation).annotatedClasses();

        //Get AnnotationStore for a_annotation
        //Canonical name of annotation store
        String annotation_store_canonical_name = ANNOTATION_STORES_PACKAGE_NAME + "." +a_annotation.getSimpleName() +AnnotationStore.ANNOTATION_SUFFIX;

        try {
            @SuppressWarnings("unchecked")
            Class<? extends AnnotationStore> annotation_store = (Class<? extends AnnotationStore>) Class.forName(annotation_store_canonical_name);

            //Create new instance
            Constructor c = annotation_store.getDeclaredConstructor();

            //Create new instance
            c.setAccessible(true);
            AnnotationStore store = (AnnotationStore) c.newInstance();

            //Add store to initialized map
            INITIALIZED_ANNOTATION_STORES.put(a_annotation, store);

            //Return
            return store.annotatedClasses();

        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new CasperException("AnnotationStore class don't have private, no args constructor.");
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            throw new CasperException(e.getMessage());
        }

    }

}
