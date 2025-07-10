package org.devnuxs.stagebuilder.processor;

import javax.lang.model.type.TypeMirror;

/**
 * Represents information about a field that will be used in stage builder generation.
 */
public class FieldInfo {
    public final String name;
    public final TypeMirror type;
    public final boolean isOptional;
    public final boolean hasDefault;
    public final String defaultValue;
    public final String defaultType;

    public FieldInfo(String name, TypeMirror type, boolean isOptional) {
        this(name, type, isOptional, false, null, null);
    }

    public FieldInfo(String name, TypeMirror type, boolean isOptional, boolean hasDefault, String defaultValue, String defaultType) {
        this.name = name;
        this.type = type;
        this.isOptional = isOptional;
        this.hasDefault = hasDefault;
        this.defaultValue = defaultValue;
        this.defaultType = defaultType;
    }
}