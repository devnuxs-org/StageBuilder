package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.CompilationRule;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Tests for the {@link StageBuilderProcessor}.
 */
public class StageBuilderProcessorTest {
    
    @Test
    public void testProcessorRecognizesAnnotation() {
        // Create a simple class with the @StageBuilder annotation
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(JavaFileObjects.forSourceString("test.Person", """
                package test;
                
                import org.devnuxs.stagebuilder.api.StageBuilder;
                
                @StageBuilder
                public class Person {
                    private String name;
                    private int age;
                    
                    public Person(String name, int age) {
                        this.name = name;
                        this.age = age;
                    }
                    
                    public String getName() { return name; }
                    public int getAge() { return age; }
                }
                """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify processor was invoked (it should generate the stage builder)
        assertThat(compilation).generatedSourceFile("test.PersonStageBuilder");
    }
    
    @Test
    public void testProcessorIgnoresNonAnnotatedClass() {
        // Create a class without the @StageBuilder annotation
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(JavaFileObjects.forSourceString("test.Person", """
                package test;
                
                public class Person {
                    private String name;
                    private int age;
                    
                    public Person(String name, int age) {
                        this.name = name;
                        this.age = age;
                    }
                    
                    public String getName() { return name; }
                    public int getAge() { return age; }
                }
                """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify processor was not invoked (no generated files)
        // Since there's no annotation, no stage builder should be generated
        assertThat(compilation).succeededWithoutWarnings();
    }
}