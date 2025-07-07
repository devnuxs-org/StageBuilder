package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Integration tests to verify end-to-end functionality.
 */
public class IntegrationTest {

    @Test
    public void testStageBuilderAnnotationProcessingIntegration() {
        // Test with a record - a common use case for builder pattern
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(JavaFileObjects.forSourceString("test.PersonRecord", """
                package test;
                
                import org.devnuxs.stagebuilder.api.StageBuilder;
                
                @StageBuilder
                public record PersonRecord(String name, int age, String email) {
                }
                """));
        
        assertThat(compilation).succeededWithoutWarnings();
        assertThat(compilation).generatedSourceFile("test.PersonRecordStageBuilder");
    }
    
    @Test
    public void testMultipleAnnotatedClasses() {
        // Test with multiple annotated classes
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.Person", """
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
                    """),
                JavaFileObjects.forSourceString("test.Address", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public record Address(String street, String city, String zipCode) {
                    }
                    """)
            );
        
        assertThat(compilation).succeededWithoutWarnings();
        assertThat(compilation).generatedSourceFile("test.PersonStageBuilder");
        assertThat(compilation).generatedSourceFile("test.AddressStageBuilder");
    }
}