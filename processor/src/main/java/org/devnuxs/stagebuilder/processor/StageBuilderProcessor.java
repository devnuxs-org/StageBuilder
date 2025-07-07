package org.devnuxs.stagebuilder.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.ParameterSpec;
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
@SupportedAnnotationTypes("org.devnuxs.stagebuilder.api.StageBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class StageBuilderProcessor extends AbstractProcessor {
    
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
                    fields.add(new FieldInfo(fieldName, fieldType));
                }
            }
        } else {
            // For classes, extract constructor parameters from the first constructor
            ExecutableElement constructor = findConstructor(element);
            if (constructor != null) {
                for (VariableElement param : constructor.getParameters()) {
                    String fieldName = param.getSimpleName().toString();
                    TypeMirror fieldType = param.asType();
                    fields.add(new FieldInfo(fieldName, fieldType));
                }
            }
        }
        
        return fields;
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
        
        for (int i = 0; i < fields.size(); i++) {
            FieldInfo field = fields.get(i);
            String interfaceName = capitalizeFirstLetter(field.name) + "Stage";
            String returnType = (i == fields.size() - 1) ? "BuildStage" : capitalizeFirstLetter(fields.get(i + 1).name) + "Stage";
            
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
        
        // Add the BuildStage interface
        TypeSpec buildStage = TypeSpec.interfaceBuilder("BuildStage")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(FunctionalInterface.class)
            .addMethod(MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(ClassName.get("", className))
                .build())
            .build();
        
        interfaces.add(buildStage);
        
        return interfaces;
    }
    
    private String getFirstStageInterfaceName(List<FieldInfo> fields) {
        if (fields.isEmpty()) {
            return "BuildStage";
        }
        return capitalizeFirstLetter(fields.get(0).name) + "Stage";
    }
    
    private TypeSpec generateBuilderInnerClass(List<FieldInfo> fields, String className, String packageName) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Builder")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        
        // Add all stage interfaces to the implements clause
        for (FieldInfo field : fields) {
            builder.addSuperinterface(ClassName.get("", capitalizeFirstLetter(field.name) + "Stage"));
        }
        builder.addSuperinterface(ClassName.get("", "BuildStage"));
        
        // Add fields
        for (FieldInfo field : fields) {
            builder.addField(FieldSpec.builder(TypeName.get(field.type), field.name, Modifier.PRIVATE).build());
        }
        
        // Add setter methods
        for (int i = 0; i < fields.size(); i++) {
            FieldInfo field = fields.get(i);
            String returnType = (i == fields.size() - 1) ? "BuildStage" : capitalizeFirstLetter(fields.get(i + 1).name) + "Stage";
            
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
        
        FieldInfo(String name, TypeMirror type) {
            this.name = name;
            this.type = type;
        }
    }
}