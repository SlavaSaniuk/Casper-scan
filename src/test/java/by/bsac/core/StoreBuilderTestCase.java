package by.bsac.core;

import by.bsac.annotations.MyAnnotation;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class StoreBuilderTestCase {

    @Test
    void withAnnotatedClassesAsString_setOfStrings_shouldCreateSourceFile() throws IOException {

        Set<String> annotated_classes_name = new HashSet<>();
        annotated_classes_name.add(HashSet.class.getCanonicalName());
        annotated_classes_name.add(DataSource.class.getCanonicalName());


        StoreBuilder sourceGen = new StoreBuilder.Builder()
                .withAnnotationName(MyAnnotation.class.getSimpleName())
                .withAnnotatedClassesAsStrings(annotated_classes_name)
                .build();

        JavaFile source_file = sourceGen.sourceFile("by.bsac.store.stores");

        source_file.writeTo(System.out);

    }
}
