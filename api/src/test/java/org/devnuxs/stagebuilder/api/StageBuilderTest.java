package org.devnuxs.stagebuilder.api;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link StageBuilder} annotation.
 */
public class StageBuilderTest {
    
    @Test
    public void testAnnotationExists() {
        // Verify the annotation exists and can be accessed
        assertNotNull(StageBuilder.class);
    }
    
    @Test
    public void testAnnotationRetention() {
        // Verify the annotation has SOURCE retention
        Retention retention = StageBuilder.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.SOURCE, retention.value());
    }
    
    @Test
    public void testAnnotationTarget() {
        // Verify the annotation targets TYPE
        Target target = StageBuilder.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertEquals(1, target.value().length);
        assertEquals(ElementType.TYPE, target.value()[0]);
    }
    
    @Test
    public void testAnnotationCanBeApplied() {
        // Test that we can apply the annotation to a class
        // Since we have SOURCE retention, we can't check at runtime
        // but we can verify the annotation compiles without errors
        @StageBuilder
        class TestClass {
        }
        
        // If we get here, the annotation was applied successfully
        // The annotation processor would be invoked at compile time
        assertNotNull(TestClass.class);
    }
}