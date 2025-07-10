package org.devnuxs.stagebuilder.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or record to have a stage builder generated for it.
 * 
 * <p>This annotation is processed at compile time to generate fluent builder 
 * classes that enforce the order of property setting through method chaining.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface StageBuilder {
    // No properties for the first version as specified
    
    /**
     * Marks a field as optional in the stage builder.
     * 
     * <p>If a field has this annotation, the strict builder for this field is not added
     * to the stage chain. After all required fields are set, all optional fields become
     * available and the build method can be called even if not all optional fields are set.</p>
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
    @interface Optional {
        // No properties needed
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
    @interface Default {
        String value() default ""; // Default value for the field, if applicable
        Class<?> type() default Object.class; // Type of the field, if applicable
    }
}