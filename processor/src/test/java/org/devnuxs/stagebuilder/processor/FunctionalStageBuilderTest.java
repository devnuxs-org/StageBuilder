package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Tests for functional stage builder generation.
 */
public class FunctionalStageBuilderTest {

    @Test
    public void testStageBuilderEnforcesStrictStageOrdering() {
        // Test that the generated stage builder enforces strict ordering
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
                // Valid usage - follows stage order
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static Person createPerson() {
                            return PersonStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void testStageBuilderPreventsSkippingStages() {
        // Test that skipping stages causes compilation errors
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
                // Invalid usage - tries to skip name stage
                JavaFileObjects.forSourceString("test.InvalidUsage", """
                    package test;
                    
                    public class InvalidUsage {
                        public static Person createPerson() {
                            return PersonStageBuilder.builder()
                                .age(30)  // This should fail - name stage was skipped
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation fails with expected error
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("cannot find symbol");
    }

    @Test
    public void testStageBuilderPreventsEarlyBuild() {
        // Test that calling build() too early causes compilation errors
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
                // Invalid usage - tries to build before all stages are complete
                JavaFileObjects.forSourceString("test.InvalidUsage", """
                    package test;
                    
                    public class InvalidUsage {
                        public static Person createPerson() {
                            return PersonStageBuilder.builder()
                                .name("John")
                                .build();  // This should fail - age stage was skipped
                        }
                    }
                    """));
        
        // Verify compilation fails with expected error
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("cannot find symbol");
    }

    @Test
    public void testRecordStageBuilderFunctionality() {
        // Test that record stage builders work correctly
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.PersonRecord", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public record PersonRecord(String name, int age, String email) {
                    }
                    """),
                JavaFileObjects.forSourceString("test.RecordUsage", """
                    package test;
                    
                    public class RecordUsage {
                        public static PersonRecord createPersonRecord() {
                            return PersonRecordStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .email("john@example.com")
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void testSingleFieldStageBuilder() {
        // Test that single field stage builders work correctly
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.SimpleClass", """
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
                    """),
                JavaFileObjects.forSourceString("test.SimpleUsage", """
                    package test;
                    
                    public class SimpleUsage {
                        public static SimpleClass createSimple() {
                            return SimpleClassStageBuilder.builder()
                                .value("test")
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }
}