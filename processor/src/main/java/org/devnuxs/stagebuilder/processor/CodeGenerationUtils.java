package org.devnuxs.stagebuilder.processor;

import java.util.List;

/**
 * Utility methods for code generation in the stage builder processor.
 */
public class CodeGenerationUtils {
    
    private static final String BUILD_STAGE = "BuildStage";
    private static final String STAGE = "Stage";
    
    /**
     * Capitalizes the first letter of a string.
     */
    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Gets the name of the first stage interface based on the fields.
     */
    public static String getFirstStageInterfaceName(List<FieldInfo> fields) {
        // Find the first required field
        for (FieldInfo field : fields) {
            if (!field.isOptional) {
                return capitalizeFirstLetter(field.name) + STAGE;
            }
        }
        // If no required fields, go directly to BuildStage
        return BUILD_STAGE;
    }
    
    /**
     * Gets the BUILD_STAGE constant.
     */
    public static String getBuildStage() {
        return BUILD_STAGE;
    }
    
    /**
     * Gets the STAGE constant.
     */
    public static String getStage() {
        return STAGE;
    }
}