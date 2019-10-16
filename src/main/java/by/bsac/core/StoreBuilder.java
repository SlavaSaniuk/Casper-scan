package by.bsac.core;

import by.bsac.store.stores.AnnotationStore;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class StoreBuilder {

    //Class fields
    private String annotation_name;
    private final Set<Class> annotated_classes = new HashSet<>();
    private final Set<String> annotated_classes_name = new HashSet<>();

    //Constructor
    private StoreBuilder() { }

    public JavaFile sourceFile(String package_name) {

        //Create source file skeleton
        TypeSpec.Builder class_builder = this.sourceFileStructureBuilder(annotation_name);

        //Add fields
        class_builder.addField(this.annotatedClassesField());

        //Add initialization methods
        class_builder.addMethod(this.importAnnotatedClasses());

        //Add constructor
        class_builder.addMethod(this.privateConstructor());

        //Implements methods
        class_builder.addMethod(this.annotatedClassesImplementedMethod());

        //Create java file
        return JavaFile.builder(package_name, class_builder.build())
                .build();
    }

    public static class Builder {

        private String annotation_name;
        private final Set<Class> annotated_classes = new HashSet<>();
        private final Set<String> annotated_classes_names = new HashSet<>();

        public Builder withAnnotationName(String a_annotation) {
            this.annotation_name = a_annotation;
            return this;
        }

        public Builder withAnnotatedClasses(Set<Class> a_annotated_classes) {
            for (Class clazz : a_annotated_classes)
                if(clazz != null) this.annotated_classes.add(clazz);
            return this;
        }

        public Builder withAnnotatedClassesAsStrings(Set<String> a_annotated_classes_names) {
            for (String class_name : a_annotated_classes_names)
                if(class_name != null && !class_name.isEmpty())
                    this.annotated_classes_names.add(class_name);
            return this;
        }

        public StoreBuilder build() {
            StoreBuilder builder = new StoreBuilder();
            builder.annotation_name = annotation_name;
            builder.annotated_classes.addAll(annotated_classes);
            builder.annotated_classes_name.addAll(annotated_classes_names);
            return builder;
        }
    }

    public String getAnnotation() {
        return annotation_name;
    }

    /**
     * Method create {@link TypeSpec.Builder} with defined class modifiers and class name.
     * Example of created source file skeleton: {@code private final class [AnnotationClassName]AnnotationStore {}}
     * @param annotation_name - {@link String} custom annotation name.
     * @return - {@link TypeSpec.Builder} builder.
     */
    private TypeSpec.Builder sourceFileStructureBuilder(String annotation_name) {

        //Generate source file class name
        String clazz_name = annotation_name  + AnnotationStore.ANNOTATION_SUFFIX;

        //Add "private final modifiers"
        //Class will implements "AnnotationStore" interface
        return TypeSpec.classBuilder(clazz_name)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(AnnotationStore.class);
    }

    private FieldSpec annotatedClassesField() {
        return FieldSpec.builder(ArrayTypeName.of(Class.class), "annotated_classes").
                addModifiers(Modifier.PRIVATE)
                .build();
    }

    private MethodSpec privateConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("this.$N = this.$N()", this.annotatedClassesField(), this.importAnnotatedClasses())
                .build();
    }

    private MethodSpec importAnnotatedClasses() {
        MethodSpec.Builder method_builder = MethodSpec.methodBuilder("importAnnotatedClasses")
                .addModifiers(Modifier.PRIVATE)
                .returns(ArrayTypeName.of(Class.class));

        method_builder.addStatement("final $T<$T> clazz_list = new $T<$T>()", List.class, Class.class, ArrayList.class, Class.class);
        for (String clazz_name : this.annotated_classes_name) {
            method_builder.addStatement("clazz_list.add($L.class)", clazz_name);
        }

        method_builder.addStatement("return clazz_list.toArray(new Class[0])");

        return method_builder.build();
    }

    private MethodSpec annotatedClassesImplementedMethod() {
        return MethodSpec.methodBuilder("annotatedClasses")
                .addModifiers(Modifier.PUBLIC)
                .returns(ArrayTypeName.of(Class.class))
                .addStatement("return this.$N", this.annotatedClassesField())
                .addAnnotation(Override.class)
                .build();
    }

}
