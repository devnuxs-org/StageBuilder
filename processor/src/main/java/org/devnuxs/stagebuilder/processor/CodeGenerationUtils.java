package org.devnuxs.stagebuilder.processor;

import com.squareup.javapoet.ClassName;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility methods for code generation in the stage builder processor.
 */
public class CodeGenerationUtils {
    
    private static final String BUILD_STAGE = "BuildStage";
    private static final String STAGE = "Stage";
    
    /**
     * Capitalizes the first letter of a string.
     * 
     * @param str the string to capitalize
     * @return the string with first letter capitalized, or the original string if null or empty
     */
    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Gets the name of the first stage interface based on the fields.
     * 
     * @param fields the list of fields to analyze
     * @return the name of the first stage interface or BuildStage if no required fields
     */
    public static String getFirstStageInterfaceName(List<FieldInfo> fields) {
        // Find the first required field (not optional and no default)
        for (FieldInfo field : fields) {
            if (!field.isOptional && !field.hasDefault) {
                return capitalizeFirstLetter(field.name) + STAGE;
            }
        }
        // If no required fields, go directly to BuildStage
        return BUILD_STAGE;
    }
    
    /**
     * Gets the BUILD_STAGE constant.
     * 
     * @return the BUILD_STAGE constant string
     */
    public static String getBuildStage() {
        return BUILD_STAGE;
    }
    
    /**
     * Gets the STAGE constant.
     * 
     * @return the STAGE constant string
     */
    public static String getStage() {
        return STAGE;
    }
    
    /**
     * Creates a ClassName for the given TypeElement, properly handling nested classes.
     * 
     * @param typeElement the type element to create a class name for
     * @param packageName the package name
     * @return a ClassName for the type element
     */
    public static ClassName getClassName(TypeElement typeElement, String packageName) {
        List<String> enclosingClassNames = new ArrayList<>();
        Element enclosing = typeElement.getEnclosingElement();
        
        // Walk up the hierarchy to find all enclosing classes
        while (enclosing != null && enclosing.getKind() == ElementKind.CLASS) {
            enclosingClassNames.add(0, enclosing.getSimpleName().toString());
            enclosing = enclosing.getEnclosingElement();
        }
        
        // If there are enclosing classes, create a nested ClassName
        if (!enclosingClassNames.isEmpty()) {
            return ClassName.get(packageName, enclosingClassNames.get(0), 
                enclosingClassNames.subList(1, enclosingClassNames.size()).toArray(new String[0]))
                .nestedClass(typeElement.getSimpleName().toString());
        } else {
            // Top-level class
            return ClassName.get(packageName, typeElement.getSimpleName().toString());
        }
    }
}