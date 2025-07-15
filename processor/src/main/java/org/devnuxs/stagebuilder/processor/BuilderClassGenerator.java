package org.devnuxs.stagebuilder.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the Builder inner class for the stage builder pattern.
 */
public class BuilderClassGenerator {

    /**
     * Generates the Builder inner class.
     * 
     * @param fields the list of fields to include in the builder
     * @param className the name of the class being built
     * @param typeElement the type element of the class being built
     * @param packageName the package name
     * @return the TypeSpec for the Builder inner class
     */
    public TypeSpec generateBuilderInnerClass(List<FieldInfo> fields, String className, TypeElement typeElement, String packageName) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Builder")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

        // Only required fields for staged interfaces and method chain
        List<FieldInfo> requiredFields = getRequiredFields(fields);
        // Only optional/default fields for BuildStage
        List<FieldInfo> optionalFields = getOptionalFields(fields);

        addSuperInterfaces(builder, requiredFields);
        addFields(builder, fields);
        addSetterMethods(builder, requiredFields, optionalFields);
        addBuildMethodSmart(builder, fields, optionalFields, className, typeElement, packageName);
        return builder.build();
    }
    
    /**
     * Generates the FromBuilder inner class that implements FromStage.
     * 
     * @param fields the list of fields to include in the builder
     * @param className the name of the class being built
     * @param typeElement the type element of the class being built
     * @param packageName the package name
     * @return the TypeSpec for the FromBuilder inner class
     */
    public TypeSpec generateFromBuilderInnerClass(List<FieldInfo> fields, String className, TypeElement typeElement, String packageName) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("FromBuilder")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .addSuperinterface(ClassName.get("", "FromStage"));

        addFields(builder, fields);
        addFromBuilderSetterMethods(builder, fields);
        addBuildMethodSmart(builder, fields, getOptionalFields(fields), className, typeElement, packageName);
        
        return builder.build();
    }
    
    private void addFromBuilderSetterMethods(TypeSpec.Builder builder, List<FieldInfo> fields) {
        for (FieldInfo field : fields) {
            MethodSpec setterMethod = MethodSpec.methodBuilder(field.name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(field.type), field.name)
                .returns(ClassName.get("", "FromStage"))
                .addStatement("this.$N = $N", field.name, field.name)
                .addStatement("return this")
                .build();
            builder.addMethod(setterMethod);
        }
    }
    
    private List<FieldInfo> getRequiredFields(List<FieldInfo> fields) {
        // Required: not optional and no default
        return fields.stream()
            .filter(field -> !field.isOptional && !field.hasDefault)
            .toList();
    }
    
    private List<FieldInfo> getOptionalFields(List<FieldInfo> fields) {
        // Optional: isOptional or hasDefault
        return fields.stream()
            .filter(field -> field.isOptional || field.hasDefault)
            .toList();
    }
    
    private void addSuperInterfaces(TypeSpec.Builder builder, List<FieldInfo> requiredFields) {
        // Only implement required stage interfaces for required (non-optional, non-default) fields
        for (FieldInfo field : requiredFields) {
            String interfaceName = CodeGenerationUtils.capitalizeFirstLetter(field.name) + 
                CodeGenerationUtils.getStage();
            builder.addSuperinterface(ClassName.get("", interfaceName));
        }
        builder.addSuperinterface(ClassName.get("", CodeGenerationUtils.getBuildStage()));
        // Don't implement FromStage in Builder to avoid method conflicts
    }
    
    private void addFields(TypeSpec.Builder builder, List<FieldInfo> fields) {
        for (FieldInfo field : fields) {
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(TypeName.get(field.type), field.name, Modifier.PRIVATE);
            if (field.hasDefault && field.defaultValue != null && !field.defaultValue.isEmpty()) {
                String defaultLiteral = field.defaultValue;
                if (field.type.toString().equals("java.lang.String")) {
                    fieldSpecBuilder.initializer("$S", defaultLiteral);
                } else {
                    fieldSpecBuilder.initializer(defaultLiteral);
                }
            }
            builder.addField(fieldSpecBuilder.build());
        }
    }
    
    private void addSetterMethods(TypeSpec.Builder builder, List<FieldInfo> requiredFields, 
                                 List<FieldInfo> optionalFields) {
        // Only required fields get staged setter methods
        addRequiredSetterMethods(builder, requiredFields);
        // Only optional/default fields get BuildStage setters
        addOptionalSetterMethods(builder, optionalFields);
        // Don't add FromStage setters to avoid conflicts
    }
    
    private void addRequiredSetterMethods(TypeSpec.Builder builder, List<FieldInfo> requiredFields) {
        for (int i = 0; i < requiredFields.size(); i++) {
            FieldInfo field = requiredFields.get(i);
            String returnType = getNextStageReturnType(requiredFields, i);
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
    }

    // Only add optional/defaulted fields to BuildStage
    private void addOptionalSetterMethods(TypeSpec.Builder builder, List<FieldInfo> optionalFields) {
        // Only add one version of this method for each optional/default field
        for (FieldInfo optionalField : optionalFields) {
            MethodSpec setterMethod = MethodSpec.methodBuilder(optionalField.name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(optionalField.type), optionalField.name)
                .returns(ClassName.get("", CodeGenerationUtils.getBuildStage()))
                .addStatement("this.$N = $N", optionalField.name, optionalField.name)
                .addStatement("return this")
                .build();
            builder.addMethod(setterMethod);
        }
    }
    

    
    private String getNextStageReturnType(List<FieldInfo> requiredFields, int currentIndex) {
        if (currentIndex == requiredFields.size() - 1) {
            return CodeGenerationUtils.getBuildStage();
        }
        return CodeGenerationUtils.capitalizeFirstLetter(requiredFields.get(currentIndex + 1).name) + 
            CodeGenerationUtils.getStage();
    }
    
    /**
     * Adds a build method that uses the all-args constructor if available, otherwise falls back to no-args constructor and setters/fields.
     */
    private void addBuildMethodSmart(TypeSpec.Builder builder, List<FieldInfo> fields, List<FieldInfo> optionalFields, String className, TypeElement typeElement, String packageName) {
        ClassName targetClassName = CodeGenerationUtils.getClassName(typeElement, packageName);
        
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(targetClassName);

        // Always try to use all-args constructor ONLY if it matches the number of fields, otherwise use no-args constructor
        boolean useAllArgsConstructor = false;
        if (!fields.isEmpty() && hasAllArgsConstructor(fields, typeElement)) {
            useAllArgsConstructor = true;
        }

        if (useAllArgsConstructor) {
            // Build constructor arg list for all fields (in order)
            StringBuilder argList = new StringBuilder();
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) argList.append(", ");
                argList.append("this.").append(fields.get(i).name);
            }
            buildMethod.addStatement("$T obj = new $T($L)", targetClassName, targetClassName, argList.toString());
        } else {
            // Use no-args constructor
            buildMethod.addStatement("$T obj = new $T()", targetClassName, targetClassName);
            // Set all fields (required, optional, default) via setters or direct access
            for (FieldInfo field : fields) {
                String setterName = "set" + Character.toUpperCase(field.name.charAt(0)) + field.name.substring(1);
                buildMethod.beginControlFlow("try")
                    .addStatement("obj.getClass().getMethod(\"$L\", $T.class).invoke(obj, this.$N)", setterName, TypeName.get(field.type), field.name)
                    .nextControlFlow("catch (Exception e)")
                    .beginControlFlow("")
                    .addStatement("try { java.lang.reflect.Field f = obj.getClass().getDeclaredField(\"$L\"); f.setAccessible(true); f.set(obj, this.$N); } catch (Exception ignore) {}", field.name, field.name)
                    .endControlFlow()
                    .endControlFlow();
            }
        }
        // If using all-args constructor, set only optional/default fields after construction
        if (useAllArgsConstructor) {
            for (FieldInfo field : optionalFields) {
                String setterName = "set" + Character.toUpperCase(field.name.charAt(0)) + field.name.substring(1);
                buildMethod.beginControlFlow("try")
                    .addStatement("obj.getClass().getMethod(\"$L\", $T.class).invoke(obj, this.$N)", setterName, TypeName.get(field.type), field.name)
                    .nextControlFlow("catch (Exception e)")
                    .beginControlFlow("")
                    .addStatement("try { java.lang.reflect.Field f = obj.getClass().getDeclaredField(\"$L\"); f.setAccessible(true); f.set(obj, this.$N); } catch (Exception ignore) {}", field.name, field.name)
                    .endControlFlow()
                    .endControlFlow();
            }
        }
        buildMethod.addStatement("return obj");
        builder.addMethod(buildMethod.build());
    }

    /**
     * Checks if the class has an all-args constructor matching the fields.
     * This is a stub for now; in a real implementation, you would analyze the class element.
     * For this fix, always return false so we use the no-args constructor for field-annotated classes.
     */
    private boolean hasAllArgsConstructor(List<FieldInfo> fields, TypeElement typeElement) {
        // Real implementation: check for a public constructor whose parameter types and order match the fields
        for (Element enclosed : typeElement.getEnclosedElements()) {
            boolean isConstructor = enclosed.getKind() == ElementKind.CONSTRUCTOR;
            ExecutableElement ctor = isConstructor ? (ExecutableElement) enclosed : null;
            boolean isPublic = isConstructor && ctor.getModifiers().contains(Modifier.PUBLIC);
            List<? extends VariableElement> params = isConstructor ? ctor.getParameters() : null;
            boolean paramCountMatches = isConstructor && params.size() == fields.size();
            boolean paramsMatch = isConstructor && paramCountMatches && paramsMatchFields(params, fields);
            if (isConstructor && isPublic && paramCountMatches && paramsMatch) {
                return true;
            }
        }
        return false;
    }

    // Helper to compare parameter types and order to fields
    private boolean paramsMatchFields(List<? extends VariableElement> params, List<FieldInfo> fields) {
        for (int i = 0; i < params.size(); i++) {
            VariableElement param = params.get(i);
            FieldInfo field = fields.get(i);
            if (!param.asType().toString().equals(field.type.toString())) {
                return false;
            }
        }
        return true;
    }
}