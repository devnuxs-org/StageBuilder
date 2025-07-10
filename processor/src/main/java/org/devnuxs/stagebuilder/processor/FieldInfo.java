package org.devnuxs.stagebuilder.processor;

import javax.lang.model.type.TypeMirror;

/**
 * Represents information about a field that will be used in stage builder generation.
 */
public class FieldInfo {
    public final String name;
    public final TypeMirror type;
    public final boolean isOptional;
    
    public FieldInfo(String name, TypeMirror type, boolean isOptional) {
        this.name = name;
        this.type = type;
        this.isOptional = isOptional;
    }
}