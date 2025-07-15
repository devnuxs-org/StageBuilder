package org.devnuxs.stagebuilder.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts field information from classes and records for stage builder generation.
 */
public class FieldExtractor {
    
    /**
     * Extracts field information from a TypeElement (class or record).
     * 
     * @param element the type element to extract fields from
     * @return a list of field information for stage builder generation
     */
    public List<FieldInfo> extractFields(TypeElement element) {
        List<FieldInfo> fields = new ArrayList<>();
        if (element.getKind() == ElementKind.RECORD) {
            fields.addAll(extractRecordFields(element));
        } else {
            List<FieldInfo> constructorFields = extractClassFields(element);
            if (!constructorFields.isEmpty()) {
                fields.addAll(constructorFields);
            } else {
                // No constructor or no-args constructor: extract from fields and/or setters directly
                java.util.Map<String, FieldInfo> fieldMap = new java.util.LinkedHashMap<>();
                // 1. Extract from all fields (private, protected, public)
                for (Element enclosedElement : element.getEnclosedElements()) {
                    if (enclosedElement.getKind() == ElementKind.FIELD) {
                        VariableElement fieldElement = (VariableElement) enclosedElement;
                        String fieldName = fieldElement.getSimpleName().toString();
                        TypeMirror fieldType = fieldElement.asType();
                        DefaultAnnotationInfo defaultInfo = getDefaultAnnotationInfo(fieldElement);
                        boolean isOptional = isAnnotatedWithOptional(fieldElement) || defaultInfo.hasDefault;
                        fieldMap.put(fieldName, new FieldInfo(fieldName, fieldType, isOptional, defaultInfo.hasDefault, defaultInfo.value, defaultInfo.type));
                    }
                }
                // 2. Merge with public setter methods (prefer field annotation if present)
                for (Element enclosedElement : element.getEnclosedElements()) {
                    if (enclosedElement.getKind() == ElementKind.METHOD && enclosedElement.getModifiers().contains(Modifier.PUBLIC)) {
                        ExecutableElement method = (ExecutableElement) enclosedElement;
                        String methodName = method.getSimpleName().toString();
                        // Only consider setters: public void setX(Type x)
                        if (methodName.startsWith("set") && method.getParameters().size() == 1 && method.getReturnType().getKind().name().equals("VOID")) {
                            String fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                            TypeMirror fieldType = method.getParameters().get(0).asType();
                            DefaultAnnotationInfo defaultInfo = getDefaultAnnotationInfo(method);
                            boolean isOptional = isAnnotatedWithOptional(method) || defaultInfo.hasDefault;
                            // If field already exists, merge: prefer field annotation for isOptional/hasDefault
                            FieldInfo existing = fieldMap.get(fieldName);
                            if (existing != null) {
                                boolean mergedOptional = existing.isOptional || isOptional;
                                boolean mergedHasDefault = existing.hasDefault || defaultInfo.hasDefault;
                                String mergedDefaultValue = existing.hasDefault ? existing.defaultValue : defaultInfo.value;
                                String mergedDefaultType = existing.hasDefault ? existing.defaultType : defaultInfo.type;
                                fieldMap.put(fieldName, new FieldInfo(fieldName, fieldType, mergedOptional, mergedHasDefault, mergedDefaultValue, mergedDefaultType));
                            } else {
                                fieldMap.put(fieldName, new FieldInfo(fieldName, fieldType, isOptional, defaultInfo.hasDefault, defaultInfo.value, defaultInfo.type));
                            }
                        }
                    }
                }
                fields.addAll(fieldMap.values());
            }
        }
        return fields;
    }
    
    private List<FieldInfo> extractRecordFields(TypeElement element) {
        List<FieldInfo> fields = new ArrayList<>();
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.RECORD_COMPONENT) {
                RecordComponentElement recordComponent = (RecordComponentElement) enclosedElement;
                String fieldName = recordComponent.getSimpleName().toString();
                TypeMirror fieldType = recordComponent.asType();
                DefaultAnnotationInfo defaultInfo = getDefaultAnnotationInfo(recordComponent);
                boolean isOptional = isAnnotatedWithOptional(recordComponent) || defaultInfo.hasDefault;
                fields.add(new FieldInfo(fieldName, fieldType, isOptional, defaultInfo.hasDefault, defaultInfo.value, defaultInfo.type));
            }
        }
        return fields;
    }
    
    private List<FieldInfo> extractClassFields(TypeElement element) {
        List<FieldInfo> fields = new ArrayList<>();
        ExecutableElement constructor = findConstructor(element);
        if (constructor != null) {
            for (VariableElement param : constructor.getParameters()) {
                String fieldName = param.getSimpleName().toString();
                TypeMirror fieldType = param.asType();
                DefaultAnnotationInfo defaultInfo = getDefaultAnnotationInfo(param);
                boolean isOptional = isAnnotatedWithOptional(param) || defaultInfo.hasDefault;
                fields.add(new FieldInfo(fieldName, fieldType, isOptional, defaultInfo.hasDefault, defaultInfo.value, defaultInfo.type));
            }
        }
        return fields;
    }
    private static class DefaultAnnotationInfo {
        boolean hasDefault;
        String value;
        String type;
        DefaultAnnotationInfo(boolean hasDefault, String value, String type) {
            this.hasDefault = hasDefault;
            this.value = value;
            this.type = type;
        }
    }

    private DefaultAnnotationInfo getDefaultAnnotationInfo(Element element) {
        for (var mirror : element.getAnnotationMirrors()) {
            if ("org.devnuxs.stagebuilder.api.StageBuilder.Default".equals(mirror.getAnnotationType().toString())) {
                String value = null;
                String type = null;
                for (var entry : mirror.getElementValues().entrySet()) {
                    String key = entry.getKey().getSimpleName().toString();
                    String val = entry.getValue().getValue().toString();
                    if ("value".equals(key)) value = val;
                    if ("type".equals(key)) type = val;
                }
                return new DefaultAnnotationInfo(true, value, type);
            }
        }
        return new DefaultAnnotationInfo(false, null, null);
    }
    
    private boolean isAnnotatedWithOptional(Element element) {
        return element.getAnnotationMirrors().stream()
            .anyMatch(mirror -> "org.devnuxs.stagebuilder.api.StageBuilder.Optional"
                .equals(mirror.getAnnotationType().toString()));
    }
    
    private ExecutableElement findConstructor(TypeElement element) {
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
}