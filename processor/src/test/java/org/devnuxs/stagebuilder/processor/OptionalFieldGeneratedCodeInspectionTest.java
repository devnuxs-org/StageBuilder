package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Manual test to inspect the generated code for optional fields.
 */
public class OptionalFieldGeneratedCodeInspectionTest {

    @Test
    public void testInspectGeneratedCodeForOptionalFields() {
        // Create a simple test case and inspect the generated code
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.Example", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public class Example {
                        private final String required1;
                        private final String required2;
                        private final String optional1;
                        private final String optional2;
                        
                        public Example(
                            String required1, 
                            String required2, 
                            @StageBuilder.Optional String optional1,
                            @StageBuilder.Optional String optional2
                        ) {
                            this.required1 = required1;
                            this.required2 = required2;
                            this.optional1 = optional1;
                            this.optional2 = optional2;
                        }
                        
                        public String getRequired1() { return required1; }
                        public String getRequired2() { return required2; }
                        public String getOptional1() { return optional1; }
                        public String getOptional2() { return optional2; }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // The generated code should contain:
        // - Required1Stage interface with required1(String) method
        // - Required2Stage interface with required2(String) method
        // - BuildStage interface with build(), optional1(String), and optional2(String) methods
        // - Builder class implementing all interfaces
        System.out.println("Generated files:");
        compilation.generatedFiles().forEach(file -> {
            System.out.println("Generated: " + file.getName());
            if (file.getName().endsWith("ExampleStageBuilder.java")) {
                try {
                    System.out.println("Content:");
                    System.out.println(file.getCharContent(true));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}