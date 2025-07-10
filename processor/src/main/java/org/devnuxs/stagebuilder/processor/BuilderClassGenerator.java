package org.devnuxs.stagebuilder.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * Generates the Builder inner class for the stage builder pattern.
 */
public class BuilderClassGenerator {
    
    /**
     * Generates the Builder inner class.
     */
    public TypeSpec generateBuilderInnerClass(List<FieldInfo> fields, String className) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Builder")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        
        List<FieldInfo> requiredFields = getRequiredFields(fields);
        List<FieldInfo> optionalFields = getOptionalFields(fields);
        
        addSuperInterfaces(builder, requiredFields);
        addFields(builder, fields);
        addSetterMethods(builder, requiredFields, optionalFields);
        addBuildMethod(builder, fields, className);
        
        return builder.build();
    }
    
    private List<FieldInfo> getRequiredFields(List<FieldInfo> fields) {
        return fields.stream()
            .filter(field -> !field.isOptional)
            .toList();
    }
    
    private List<FieldInfo> getOptionalFields(List<FieldInfo> fields) {
        return fields.stream()
            .filter(field -> field.isOptional)
            .toList();
    }
    
    private void addSuperInterfaces(TypeSpec.Builder builder, List<FieldInfo> requiredFields) {
        for (FieldInfo field : requiredFields) {
            String interfaceName = CodeGenerationUtils.capitalizeFirstLetter(field.name) + 
                CodeGenerationUtils.getStage();
            builder.addSuperinterface(ClassName.get("", interfaceName));
        }
        builder.addSuperinterface(ClassName.get("", CodeGenerationUtils.getBuildStage()));
    }
    
    private void addFields(TypeSpec.Builder builder, List<FieldInfo> fields) {
        for (FieldInfo field : fields) {
            FieldSpec fieldSpec = FieldSpec.builder(TypeName.get(field.type), field.name, Modifier.PRIVATE)
                .build();
            builder.addField(fieldSpec);
        }
    }
    
    private void addSetterMethods(TypeSpec.Builder builder, List<FieldInfo> requiredFields, 
                                 List<FieldInfo> optionalFields) {
        addRequiredSetterMethods(builder, requiredFields);
        addOptionalSetterMethods(builder, optionalFields);
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
    
    private void addOptionalSetterMethods(TypeSpec.Builder builder, List<FieldInfo> optionalFields) {
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
    
    private void addBuildMethod(TypeSpec.Builder builder, List<FieldInfo> fields, String className) {
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(ClassName.get("", className));
        
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
    }
}