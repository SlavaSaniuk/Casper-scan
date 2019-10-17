package by.bsac.core;

import by.bsac.annotations.MyAnnotation;
import by.bsac.store.CustomAnnotationStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@MyAnnotation
class CustomAnnotationStoreTestCase {

    @Test
    void getClassesAnnotatedWith_annotatedClasses_shouldReturnClassArray() {

        Class[] annotated_classes = CustomAnnotationStore.getClassesAnnotatedWith(MyAnnotation.class);

        Assertions.assertNotNull(annotated_classes);

        System.out.println(Arrays.toString(annotated_classes));


    }
}
