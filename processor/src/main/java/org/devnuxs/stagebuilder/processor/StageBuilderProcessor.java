package org.devnuxs.stagebuilder.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
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
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
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
    
    private static final String BUILD_STAGE = "BuildStage";
    private static final String STAGE = "Stage";

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
        
        // Extract fields from the class or record
        List<FieldInfo> fields = extractFields(element);
        
        if (fields.isEmpty()) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "No fields found for stage builder generation in " + className,
                element
            );
            return;
        }
        
        // Generate the stage builder class
        TypeSpec.Builder builderClass = TypeSpec.classBuilder(builderClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        
        // Generate interfaces for each stage
        List<TypeSpec> stageInterfaces = generateStageInterfaces(fields, className);
        builderClass.addTypes(stageInterfaces);
        
        // Generate the static builder() method
        MethodSpec builderMethod = MethodSpec.methodBuilder("builder")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(ClassName.get(packageName, builderClassName, getFirstStageInterfaceName(fields)))
            .addStatement("return new Builder()")
            .build();
        builderClass.addMethod(builderMethod);
        
        // Generate the Builder inner class
        TypeSpec builderInnerClass = generateBuilderInnerClass(fields, className, packageName);
        builderClass.addType(builderInnerClass);
        
        // Write the generated class to a file
        JavaFile javaFile = JavaFile.builder(packageName, builderClass.build()).build();
        javaFile.writeTo(processingEnv.getFiler());
    }
    
    private List<FieldInfo> extractFields(TypeElement element) {
        List<FieldInfo> fields = new ArrayList<>();
        
        if (element.getKind() == ElementKind.RECORD) {
            // For records, extract record components
            for (Element enclosedElement : element.getEnclosedElements()) {
                if (enclosedElement.getKind() == ElementKind.RECORD_COMPONENT) {
                    RecordComponentElement recordComponent = (RecordComponentElement) enclosedElement;
                    String fieldName = recordComponent.getSimpleName().toString();
                    TypeMirror fieldType = recordComponent.asType();
                    boolean isOptional = isAnnotatedWithOptional(recordComponent);
                    fields.add(new FieldInfo(fieldName, fieldType, isOptional));
                }
            }
        } else {
            // For classes, extract constructor parameters from the first constructor
            ExecutableElement constructor = findConstructor(element);
            if (constructor != null) {
                for (VariableElement param : constructor.getParameters()) {
                    String fieldName = param.getSimpleName().toString();
                    TypeMirror fieldType = param.asType();
                    boolean isOptional = isAnnotatedWithOptional(param);
                    fields.add(new FieldInfo(fieldName, fieldType, isOptional));
                }
            }
        }
        
        return fields;
    }
    
    private boolean isAnnotatedWithOptional(Element element) {
        return element.getAnnotationMirrors().stream()
            .anyMatch(mirror -> "org.devnuxs.stagebuilder.api.StageBuilder.Optional".equals(mirror.getAnnotationType().toString()));
    }
    
    private ExecutableElement findConstructor(TypeElement element) {
        // Find the first constructor - in a real implementation, we might want to find the "best" constructor
        // or allow annotation parameters to specify which constructor to use
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement) enclosedElement;
                if (constructor.getModifiers().contains(Modifier.PUBLIC)) {
                    return constructor;
                }
            }
        }
        return null;
    }
    
    private List<TypeSpec> generateStageInterfaces(List<FieldInfo> fields, String className) {
        List<TypeSpec> interfaces = new ArrayList<>();
        
        // Separate required and optional fields
        List<FieldInfo> requiredFields = fields.stream()
            .filter(field -> !field.isOptional)
            .toList();
        List<FieldInfo> optionalFields = fields.stream()
            .filter(field -> field.isOptional)
            .toList();
        
        // Generate stage interfaces for required fields only
        for (int i = 0; i < requiredFields.size(); i++) {
            FieldInfo field = requiredFields.get(i);
            String interfaceName = capitalizeFirstLetter(field.name) + STAGE;
            String returnType = (i == requiredFields.size() - 1) ? BUILD_STAGE : capitalizeFirstLetter(requiredFields.get(i + 1).name) + STAGE;
            
            TypeSpec stageInterface = TypeSpec.interfaceBuilder(interfaceName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(FunctionalInterface.class)
                .addMethod(MethodSpec.methodBuilder(field.name)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(TypeName.get(field.type), field.name)
                    .returns(ClassName.get("", returnType))
                    .build())
                .build();
            
            interfaces.add(stageInterface);
        }
        
        // Generate the BuildStage interface with optional field methods
        TypeSpec.Builder buildStageBuilder = TypeSpec.interfaceBuilder(BUILD_STAGE)
            .addModifiers(Modifier.PUBLIC);
        
        // Only add @FunctionalInterface if there are no optional fields
        if (optionalFields.isEmpty()) {
            buildStageBuilder.addAnnotation(FunctionalInterface.class);
        }
        
        // Add build method
        buildStageBuilder.addMethod(MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(ClassName.get("", className))
            .build());
        
        // Add optional field methods
        for (FieldInfo optionalField : optionalFields) {
            buildStageBuilder.addMethod(MethodSpec.methodBuilder(optionalField.name)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(TypeName.get(optionalField.type), optionalField.name)
                .returns(ClassName.get("", BUILD_STAGE))
                .build());
        }
        
        interfaces.add(buildStageBuilder.build());
        
        return interfaces;
    }
    
    private String getFirstStageInterfaceName(List<FieldInfo> fields) {
        // Find the first required field
        for (FieldInfo field : fields) {
            if (!field.isOptional) {
                return capitalizeFirstLetter(field.name) + STAGE;
            }
        }
        // If no required fields, go directly to BuildStage
        return BUILD_STAGE;
    }
    
    private TypeSpec generateBuilderInnerClass(List<FieldInfo> fields, String className, String packageName) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Builder")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        
        // Separate required and optional fields
        List<FieldInfo> requiredFields = fields.stream()
            .filter(field -> !field.isOptional)
            .toList();
        List<FieldInfo> optionalFields = fields.stream()
            .filter(field -> field.isOptional)
            .toList();
        
        // Add all stage interfaces to the implements clause (only required fields)
        for (FieldInfo field : requiredFields) {
            builder.addSuperinterface(ClassName.get("", capitalizeFirstLetter(field.name) + STAGE));
        }
        builder.addSuperinterface(ClassName.get("", BUILD_STAGE));
        
        // Add fields for all fields (required and optional)
        for (FieldInfo field : fields) {
            builder.addField(FieldSpec.builder(TypeName.get(field.type), field.name, Modifier.PRIVATE).build());
        }
        
        // Add setter methods for required fields
        for (int i = 0; i < requiredFields.size(); i++) {
            FieldInfo field = requiredFields.get(i);
            String returnType = (i == requiredFields.size() - 1) ? BUILD_STAGE : capitalizeFirstLetter(requiredFields.get(i + 1).name) + STAGE;
            
            MethodSpec setterMethod = MethodSpec.methodBuilder(field.name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(field.type), field.name)
                .returns(ClassName.get("", returnType))
                .addStatement("this.$N = $N", field.name, field.name)
                .addStatement("return this")
                .build();
            
            builder.addMethod(setterMethod);
        }
        
        // Add setter methods for optional fields
        for (FieldInfo optionalField : optionalFields) {
            MethodSpec setterMethod = MethodSpec.methodBuilder(optionalField.name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(optionalField.type), optionalField.name)
                .returns(ClassName.get("", BUILD_STAGE))
                .addStatement("this.$N = $N", optionalField.name, optionalField.name)
                .addStatement("return this")
                .build();
            
            builder.addMethod(setterMethod);
        }
        
        // Add build method
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(ClassName.get("", className));
        
        // Generate constructor call
        StringBuilder constructorCall = new StringBuilder("return new $T(");
        Object[] args = new Object[fields.size() + 1];
        args[0] = ClassName.get("", className);
        
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                constructorCall.append(", ");
            }
            constructorCall.append("$N");
            args[i + 1] = fields.get(i).name;
        }
        constructorCall.append(")");
        
        buildMethod.addStatement(constructorCall.toString(), args);
        builder.addMethod(buildMethod.build());
        
        return builder.build();
    }
    
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    private static class FieldInfo {
        final String name;
        final TypeMirror type;
        final boolean isOptional;
        
        FieldInfo(String name, TypeMirror type, boolean isOptional) {
            this.name = name;
            this.type = type;
            this.isOptional = isOptional;
        }
    }
}