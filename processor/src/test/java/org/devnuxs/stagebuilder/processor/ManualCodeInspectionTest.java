package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.List;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Test to manually inspect generated code.
 */
public class ManualCodeInspectionTest {

    @Test
    public void testGeneratedCodeInspection() throws IOException {
        // Create a simple test case
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(JavaFileObjects.forSourceString("test.SimpleUser", """
                package test;
                
                import org.devnuxs.stagebuilder.api.StageBuilder;
                
                @StageBuilder
                public class SimpleUser {
                    private final String name;
                    private final int age;
                    
                    public SimpleUser(String name, int age) {
                        this.name = name;
                        this.age = age;
                    }
                    
                    public String getName() { return name; }
                    public int getAge() { return age; }
                }
                """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation).generatedSourceFile("test.SimpleUserStageBuilder");
        
        // Print the generated code for manual inspection
        var generatedFiles = compilation.generatedFiles();
        for (var file : generatedFiles) {
            if (file.getName().contains("SimpleUserStageBuilder")) {
                System.out.println("Generated SimpleUserStageBuilder:");
                System.out.println("=====================================");
                System.out.println(file.getCharContent(false));
                System.out.println("=====================================");
            }
        }
    }
}