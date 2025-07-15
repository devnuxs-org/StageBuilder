package org.devnuxs.stagebuilder.processor;

import javax.lang.model.type.TypeMirror;

/**
 * Represents information about a field that will be used in stage builder generation.
 */
public class FieldInfo {
    /** The name of the field. */
    public final String name;
    /** The type of the field. */
    public final TypeMirror type;
    /** Whether the field is optional in the builder pattern. */
    public final boolean isOptional;
    /** Whether the field has a default value. */
    public final boolean hasDefault;
    /** The default value as a string representation. */
    public final String defaultValue;
    /** The type of the default value. */
    public final String defaultType;

    /**
     * Creates a FieldInfo without default value information.
     * 
     * @param name the name of the field
     * @param type the type of the field
     * @param isOptional whether the field is optional
     */
    public FieldInfo(String name, TypeMirror type, boolean isOptional) {
        this(name, type, isOptional, false, null, null);
    }

    /**
     * Creates a FieldInfo with complete information including default values.
     * 
     * @param name the name of the field
     * @param type the type of the field
     * @param isOptional whether the field is optional
     * @param hasDefault whether the field has a default value
     * @param defaultValue the default value as a string
     * @param defaultType the type of the default value
     */
    public FieldInfo(String name, TypeMirror type, boolean isOptional, boolean hasDefault, String defaultValue, String defaultType) {
        this.name = name;
        this.type = type;
        this.isOptional = isOptional;
        this.hasDefault = hasDefault;
        this.defaultValue = defaultValue;
        this.defaultType = defaultType;
    }
}