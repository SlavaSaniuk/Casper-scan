# Casper
Java library that's suggest simple acceess to user-defined custom annotations in runtime. 

How to enable scan of your custom annotation?
  1) Create custom annotation class and annotate it's with @Custom annotation.
  2) Enable "annotation processing" in your IDE settings.

How to get all classes, annotated with user-defined custom annotation?
  1) Enable scan of your custom annotation.
  2) Call the static method getClassesAnnotatedWith(Class annotation_class) of class CustomAnnotationStore class. This method return array of clasess, annotated with custom annotation.
