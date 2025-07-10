package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Tests for default value functionality in stage builders.
 */
class DefaultFieldStageBuilderTest {

    @Test
    void testDefaultFieldInClass() {
        // Test that default fields work correctly in classes
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
                        private final String email;
                        
                        public Person(String name, int age, @StageBuilder.Default(value = \"default@email.com\") String email) {
                            this.name = name;
                            this.age = age;
                            this.email = email;
                        }
                        
                        public String getName() { return name; }
                        public int getAge() { return age; }
                        public String getEmail() { return email; }
                    }
                    """),
                // Valid usage - builds without setting default field
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static Person createPersonWithoutEmail() {
                            return PersonStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .build();
                        }
                        public static Person createPersonWithEmail() {
                            return PersonStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .email("john@example.com")
                                .build();
                        }
                    }
                    """));
        // Debug output removed after confirming green build
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    void testDefaultFieldInRecord() {
        // Test that default fields work correctly in records
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.PersonRecord", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public record PersonRecord(String name, int age, @StageBuilder.Default(value = \"default@email.com\") String email) {
                    }
                    """),
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static PersonRecord createPersonWithoutEmail() {
                            return PersonRecordStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .build();
                        }
                        public static PersonRecord createPersonWithEmail() {
                            return PersonRecordStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .email("john@example.com")
                                .build();
                        }
                    }
                    """));
        // Debug output removed after confirming green build
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }
}
