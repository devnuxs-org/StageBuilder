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
     */
    public List<FieldInfo> extractFields(TypeElement element) {
        List<FieldInfo> fields = new ArrayList<>();
        
        if (element.getKind() == ElementKind.RECORD) {
            fields.addAll(extractRecordFields(element));
        } else {
            fields.addAll(extractClassFields(element));
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
                boolean isOptional = isAnnotatedWithOptional(recordComponent);
                fields.add(new FieldInfo(fieldName, fieldType, isOptional));
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
                boolean isOptional = isAnnotatedWithOptional(param);
                fields.add(new FieldInfo(fieldName, fieldType, isOptional));
            }
        }
        
        return fields;
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