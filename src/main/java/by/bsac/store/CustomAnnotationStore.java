package by.bsac.store;

public class CustomAnnotationStore {

    public static Class[] getClassesAnnotatedWith(Class a_annotation) {

        //Check annotation parameter
        if (a_annotation == null) throw new NullPointerException("Annotation method parameter is null.");
        if (!a_annotation.isAnnotation()) throw new IllegalArgumentException("Annotation method parameter is not a annotation.");

        return null;
    }

}
