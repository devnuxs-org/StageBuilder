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
}