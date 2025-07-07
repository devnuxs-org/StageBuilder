package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Tests for basic stage builder generation functionality.
 */
public class BasicStageBuilderGenerationTest {

    @Test
    public void testSimpleClassStageBuilderGenerationCompiles() {
        // Test that the processor generates code that compiles successfully
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(JavaFileObjects.forSourceString("test.Person", """
                package test;
                
                import org.devnuxs.stagebuilder.api.StageBuilder;
                
                @StageBuilder
                public class Person {
                    private final String name;
                    private final int age;
                    
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
        
        // Verify the generated stage builder class exists
        assertThat(compilation)
            .generatedSourceFile("test.PersonStageBuilder");
    }

    @Test
    public void testRecordStageBuilderGenerationCompiles() {
        // Test that the processor generates code that compiles successfully for records
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(JavaFileObjects.forSourceString("test.PersonRecord", """
                package test;
                
                import org.devnuxs.stagebuilder.api.StageBuilder;
                
                @StageBuilder
                public record PersonRecord(String name, int age, String email) {
                }
                """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation)
            .generatedSourceFile("test.PersonRecordStageBuilder");
    }

    @Test
    public void testSingleFieldClassStageBuilderGenerationCompiles() {
        // Test that the processor generates code that compiles successfully for single field
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(JavaFileObjects.forSourceString("test.SimpleClass", """
                package test;
                
                import org.devnuxs.stagebuilder.api.StageBuilder;
                
                @StageBuilder
                public class SimpleClass {
                    private final String value;
                    
                    public SimpleClass(String value) {
                        this.value = value;
                    }
                    
                    public String getValue() { return value; }
                }
                """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation)
            .generatedSourceFile("test.SimpleClassStageBuilder");
    }

    @Test
    public void testUsageOfGeneratedStageBuilder() {
        // Test that we can use the generated stage builder
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.Person", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public class Person {
                        private final String name;
                        private final int age;
                        
                        public Person(String name, int age) {
                            this.name = name;
                            this.age = age;
                        }
                        
                        public String getName() { return name; }
                        public int getAge() { return age; }
                    }
                    """),
                JavaFileObjects.forSourceString("test.PersonUsage", """
                    package test;
                    
                    public class PersonUsage {
                        public static void main(String[] args) {
                            // Test the staged builder works
                            Person person = PersonStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .build();
                            
                            System.out.println("Name: " + person.getName());
                            System.out.println("Age: " + person.getAge());
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }
}