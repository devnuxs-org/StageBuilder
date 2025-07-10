package org.devnuxs.stagebuilder.processor;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the refactored classes to ensure maintainability improvements work correctly.
 */
public class RefactoringValidationTest {
    
    @Test
    public void testCodeGenerationUtilsCapitalization() {
        assertEquals("TestField", CodeGenerationUtils.capitalizeFirstLetter("testField"));
        assertEquals("", CodeGenerationUtils.capitalizeFirstLetter(""));
        assertNull(CodeGenerationUtils.capitalizeFirstLetter(null));
    }
    
    @Test
    public void testCodeGenerationUtilsConstants() {
        assertEquals("BuildStage", CodeGenerationUtils.getBuildStage());
        assertEquals("Stage", CodeGenerationUtils.getStage());
    }
    
    @Test
    public void testGetFirstStageInterfaceNameWithOnlyOptionalFields() {
        // Test with only optional fields - should return BuildStage
        List<FieldInfo> fieldsOnlyOptional = Arrays.asList(
            new FieldInfo("name", null, true),
            new FieldInfo("age", null, true)
        );
        assertEquals("BuildStage", CodeGenerationUtils.getFirstStageInterfaceName(fieldsOnlyOptional));
    }
    
    @Test
    public void testGetFirstStageInterfaceNameWithRequiredFields() {
        // Test with at least one required field
        List<FieldInfo> fieldsWithRequired = Arrays.asList(
            new FieldInfo("name", null, false),
            new FieldInfo("age", null, true)
        );
        assertEquals("NameStage", CodeGenerationUtils.getFirstStageInterfaceName(fieldsWithRequired));
    }
}