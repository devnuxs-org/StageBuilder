package org.devnuxs.stagebuilder.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.devnuxs.stagebuilder.api.StageBuilder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Annotation processor for the {@link StageBuilder} annotation.
 * 
 * <p>This processor recognizes classes and records annotated with {@code @StageBuilder}
 * and will generate fluent builder classes for them.</p>
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"org.devnuxs.stagebuilder.api.StageBuilder", "org.devnuxs.stagebuilder.api.StageBuilder.Optional"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class StageBuilderProcessor extends AbstractProcessor {
    
    private final FieldExtractor fieldExtractor = new FieldExtractor();
    private final StageInterfaceGenerator stageInterfaceGenerator = new StageInterfaceGenerator();
    private final BuilderClassGenerator builderClassGenerator = new BuilderClassGenerator();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Get all elements annotated with @StageBuilder
        var annotatedElements = roundEnv.getElementsAnnotatedWith(StageBuilder.class);
        
        for (var element : annotatedElements) {
            if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.RECORD) {
                try {
                    generateStageBuilder((TypeElement) element);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Failed to generate stage builder for " + element.getSimpleName() + ": " + e.getMessage(),
                        element
                    );
                }
            }
        }
        
        return true;
    }
    
    private void generateStageBuilder(TypeElement element) throws IOException {
        String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        String className = element.getSimpleName().toString();
        String builderClassName = className + "StageBuilder";
        
        List<FieldInfo> fields = fieldExtractor.extractFields(element);
        
        if (fields.isEmpty()) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "No fields found for stage builder generation in " + className,
                element
            );
            return;
        }
        
        TypeSpec builderClass = createBuilderClass(builderClassName, fields, className, packageName, element);
        
        JavaFile javaFile = JavaFile.builder(packageName, builderClass).build();
        javaFile.writeTo(processingEnv.getFiler());
    }
    
    private TypeSpec createBuilderClass(String builderClassName, List<FieldInfo> fields, 
                                       String className, String packageName, TypeElement element) {
        TypeSpec.Builder builderClass = TypeSpec.classBuilder(builderClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        
        List<TypeSpec> stageInterfaces = stageInterfaceGenerator.generateStageInterfaces(fields, className, element, packageName);
        builderClass.addTypes(stageInterfaces);
        
        MethodSpec builderMethod = createBuilderMethod(fields, packageName, builderClassName);
        builderClass.addMethod(builderMethod);
        
        MethodSpec fromMethod = createFromMethod(fields, className, packageName, builderClassName, element);
        builderClass.addMethod(fromMethod);
        
        // Add helper method for reflection-based field access
        MethodSpec getFieldValueMethod = createGetFieldValueMethod();
        builderClass.addMethod(getFieldValueMethod);
        
        TypeSpec builderInnerClass = builderClassGenerator.generateBuilderInnerClass(fields, className, element, packageName);
        builderClass.addType(builderInnerClass);
        
        TypeSpec fromBuilderInnerClass = builderClassGenerator.generateFromBuilderInnerClass(fields, className, element, packageName);
        builderClass.addType(fromBuilderInnerClass);
        
        return builderClass.build();
    }
    
    private MethodSpec createBuilderMethod(List<FieldInfo> fields, String packageName, String builderClassName) {
        return MethodSpec.methodBuilder("builder")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(ClassName.get(packageName, builderClassName, 
                CodeGenerationUtils.getFirstStageInterfaceName(fields)))
            .addStatement("return new Builder()")
            .build();
    }
    
    private MethodSpec createFromMethod(List<FieldInfo> fields, String className, String packageName, 
                                      String builderClassName, TypeElement element) {
        ClassName targetClassName = CodeGenerationUtils.getClassName(element, packageName);
        ClassName fromStageClassName = ClassName.get(packageName, builderClassName, "FromStage");
        
        MethodSpec.Builder fromMethod = MethodSpec.methodBuilder("from")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(targetClassName, "obj")
            .returns(fromStageClassName)
            .addStatement("FromBuilder builder = new FromBuilder()");
        
        // Add field extraction logic based on whether it's a record or class
        if (element.getKind() == ElementKind.RECORD) {
            // For records, use accessor methods: obj.fieldName()
            for (FieldInfo field : fields) {
                fromMethod.addStatement("builder.$N = obj.$N()", field.name, field.name);
            }
        } else {
            // For classes, determine the best access method for each field
            for (FieldInfo field : fields) {
                String accessCode = determineFieldAccessCode(field, element);
                fromMethod.addStatement("builder.$N = $L", field.name, accessCode);
            }
        }
        
        fromMethod.addStatement("return builder");
        return fromMethod.build();
    }
    
    private String determineFieldAccessCode(FieldInfo fieldInfo, TypeElement typeElement) {
        String fieldName = fieldInfo.name;
        
        // Check if there's a public getter method
        String getterName = "get" + CodeGenerationUtils.capitalizeFirstLetter(fieldName);
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) enclosed;
                if (method.getSimpleName().toString().equals(getterName) && 
                    method.getModifiers().contains(Modifier.PUBLIC) &&
                    method.getParameters().isEmpty()) {
                    return "obj." + getterName + "()";
                }
            }
        }
        
        // Check if there's a public field
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) enclosed;
                if (field.getSimpleName().toString().equals(fieldName) && 
                    field.getModifiers().contains(Modifier.PUBLIC)) {
                    return "obj." + fieldName;
                }
            }
        }
        
        // Fallback to reflection for private fields
        return String.format("(%s) getFieldValue(obj, \"%s\")", fieldInfo.type.toString(), fieldName);
    }
    
    private MethodSpec createGetFieldValueMethod() {
        return MethodSpec.methodBuilder("getFieldValue")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addParameter(Object.class, "obj")
            .addParameter(String.class, "fieldName")
            .returns(Object.class)
            .beginControlFlow("try")
            .addStatement("java.lang.reflect.Field f = obj.getClass().getDeclaredField(fieldName)")
            .addStatement("f.setAccessible(true)")
            .addStatement("return f.get(obj)")
            .nextControlFlow("catch (Exception e)")
            .addStatement("throw new RuntimeException(\"Unable to access field '\" + fieldName + \"' from object\", e)")
            .endControlFlow()
            .build();
    }
}