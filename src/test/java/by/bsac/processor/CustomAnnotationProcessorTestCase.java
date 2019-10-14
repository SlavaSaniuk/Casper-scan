package by.bsac.processor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CustomAnnotationProcessorTestCase {

    @Test
    void process_annotationAnnotatedWithCustom_shouldShowMessageInBuildLog() {
        boolean its_true = true;
        Assertions.assertTrue(its_true);
    }
}
