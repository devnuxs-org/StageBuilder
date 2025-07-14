package org.devnuxs.stagebuilder.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates stage interfaces for the stage builder pattern.
 */
public class StageInterfaceGenerator {
    
    /**
     * Generates all stage interfaces for the given fields.
     */
    public List<TypeSpec> generateStageInterfaces(List<FieldInfo> fields, String className, TypeElement typeElement, String packageName) {
        List<TypeSpec> interfaces = new ArrayList<>();

        // Required: not optional and no default
        List<FieldInfo> requiredFields = fields.stream()
            .filter(field -> !field.isOptional && !field.hasDefault)
            .toList();
        // Optional: isOptional or hasDefault
        List<FieldInfo> optionalFields = fields.stream()
            .filter(field -> field.isOptional || field.hasDefault)
            .toList();

        // If there are no required fields, allow all fields in BuildStage (any order)
        if (requiredFields.isEmpty()) {
            interfaces.add(generateBuildStageInterface(fields, className, typeElement, packageName));
        } else {
            interfaces.addAll(generateRequiredStageInterfaces(requiredFields));
            interfaces.add(generateBuildStageInterface(optionalFields, className, typeElement, packageName));
        }

        // Always add FromStage interface that allows setting any field
        interfaces.add(generateFromStageInterface(fields, className, typeElement, packageName));

        return interfaces;
    }
    
    
    private List<TypeSpec> generateRequiredStageInterfaces(List<FieldInfo> requiredFields) {
        List<TypeSpec> interfaces = new ArrayList<>();
        
        for (int i = 0; i < requiredFields.size(); i++) {
            FieldInfo field = requiredFields.get(i);
            String interfaceName = CodeGenerationUtils.capitalizeFirstLetter(field.name) + 
                CodeGenerationUtils.getStage();
            String returnType = getNextStageReturnType(requiredFields, i);
            
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
        
        return interfaces;
    }
    
    private String getNextStageReturnType(List<FieldInfo> requiredFields, int currentIndex) {
        if (currentIndex == requiredFields.size() - 1) {
            return CodeGenerationUtils.getBuildStage();
        }
        return CodeGenerationUtils.capitalizeFirstLetter(requiredFields.get(currentIndex + 1).name) + 
            CodeGenerationUtils.getStage();
    }
    
    private TypeSpec generateBuildStageInterface(List<FieldInfo> optionalFields, String className, TypeElement typeElement, String packageName) {
        TypeSpec.Builder buildStageBuilder = TypeSpec.interfaceBuilder(CodeGenerationUtils.getBuildStage())
            .addModifiers(Modifier.PUBLIC);
        
        if (optionalFields.isEmpty()) {
            buildStageBuilder.addAnnotation(FunctionalInterface.class);
        }
        
        buildStageBuilder.addMethod(generateBuildMethod(className, typeElement, packageName));
        
        for (FieldInfo optionalField : optionalFields) {
            buildStageBuilder.addMethod(generateOptionalFieldMethod(optionalField));
        }
        
        return buildStageBuilder.build();
    }
    
    private MethodSpec generateBuildMethod(String className, TypeElement typeElement, String packageName) {
        return MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(CodeGenerationUtils.getClassName(typeElement, packageName))
            .build();
    }
    
    private MethodSpec generateOptionalFieldMethod(FieldInfo optionalField) {
        return MethodSpec.methodBuilder(optionalField.name)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(TypeName.get(optionalField.type), optionalField.name)
            .returns(ClassName.get("", CodeGenerationUtils.getBuildStage()))
            .build();
    }
    
    private TypeSpec generateFromStageInterface(List<FieldInfo> allFields, String className, TypeElement typeElement, String packageName) {
        TypeSpec.Builder fromStageBuilder = TypeSpec.interfaceBuilder("FromStage")
            .addModifiers(Modifier.PUBLIC);
        
        // Add build method
        fromStageBuilder.addMethod(generateBuildMethod(className, typeElement, packageName));
        
        // Add setter methods for ALL fields (required and optional)
        for (FieldInfo field : allFields) {
            fromStageBuilder.addMethod(generateFromStageFieldMethod(field));
        }
        
        return fromStageBuilder.build();
    }
    
    private MethodSpec generateFromStageFieldMethod(FieldInfo field) {
        return MethodSpec.methodBuilder(field.name)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(TypeName.get(field.type), field.name)
            .returns(ClassName.get("", "FromStage"))
            .build();
    }
}