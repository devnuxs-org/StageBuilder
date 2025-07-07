package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Tests for optional field functionality in stage builders.
 */
public class OptionalFieldStageBuilderTest {

    @Test
    public void testOptionalFieldInClass() {
        // Test that optional fields work correctly in classes
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
                        
                        public Person(String name, int age, @StageBuilder.Optional String email) {
                            this.name = name;
                            this.age = age;
                            this.email = email;
                        }
                        
                        public String getName() { return name; }
                        public int getAge() { return age; }
                        public String getEmail() { return email; }
                    }
                    """),
                // Valid usage - builds without setting optional field
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
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void testOptionalFieldInRecord() {
        // Test that optional fields work correctly in records
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.PersonRecord", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public record PersonRecord(String name, int age, @StageBuilder.Optional String email) {
                    }
                    """),
                // Valid usage - builds without setting optional field
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
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void testMultipleOptionalFields() {
        // Test multiple optional fields
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.Person", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public class Person {
                        private final String name;
                        private final String email;
                        private final String phone;
                        
                        public Person(String name, @StageBuilder.Optional String email, @StageBuilder.Optional String phone) {
                            this.name = name;
                            this.email = email;
                            this.phone = phone;
                        }
                        
                        public String getName() { return name; }
                        public String getEmail() { return email; }
                        public String getPhone() { return phone; }
                    }
                    """),
                // Valid usage - various combinations
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static Person createPersonMinimal() {
                            return PersonStageBuilder.builder()
                                .name("John")
                                .build();
                        }
                        
                        public static Person createPersonWithEmail() {
                            return PersonStageBuilder.builder()
                                .name("John")
                                .email("john@example.com")
                                .build();
                        }
                        
                        public static Person createPersonWithPhone() {
                            return PersonStageBuilder.builder()
                                .name("John")
                                .phone("555-1234")
                                .build();
                        }
                        
                        public static Person createPersonFull() {
                            return PersonStageBuilder.builder()
                                .name("John")
                                .email("john@example.com")
                                .phone("555-1234")
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void testAllOptionalFields() {
        // Test that a class with all optional fields works correctly
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.Settings", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public class Settings {
                        private final boolean darkMode;
                        private final String theme;
                        
                        public Settings(@StageBuilder.Optional boolean darkMode, @StageBuilder.Optional String theme) {
                            this.darkMode = darkMode;
                            this.theme = theme;
                        }
                        
                        public boolean isDarkMode() { return darkMode; }
                        public String getTheme() { return theme; }
                    }
                    """),
                // Valid usage - can build immediately
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static Settings createEmptySettings() {
                            return SettingsStageBuilder.builder()
                                .build();
                        }
                        
                        public static Settings createDarkModeSettings() {
                            return SettingsStageBuilder.builder()
                                .darkMode(true)
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void testOptionalFieldOrderingFlexibility() {
        // Test that optional fields can be set in any order
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.Person", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public class Person {
                        private final String name;
                        private final String email;
                        private final String phone;
                        private final String address;
                        
                        public Person(String name, @StageBuilder.Optional String email, @StageBuilder.Optional String phone, @StageBuilder.Optional String address) {
                            this.name = name;
                            this.email = email;
                            this.phone = phone;
                            this.address = address;
                        }
                        
                        public String getName() { return name; }
                        public String getEmail() { return email; }
                        public String getPhone() { return phone; }
                        public String getAddress() { return address; }
                    }
                    """),
                // Valid usage - different orders
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static Person createPersonOrder1() {
                            return PersonStageBuilder.builder()
                                .name("John")
                                .email("john@example.com")
                                .phone("555-1234")
                                .address("123 Main St")
                                .build();
                        }
                        
                        public static Person createPersonOrder2() {
                            return PersonStageBuilder.builder()
                                .name("John")
                                .address("123 Main St")
                                .email("john@example.com")
                                .phone("555-1234")
                                .build();
                        }
                        
                        public static Person createPersonOrder3() {
                            return PersonStageBuilder.builder()
                                .name("John")
                                .phone("555-1234")
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void testRequiredFieldsStillEnforceOrder() {
        // Test that required fields still need to be set in order
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
                        
                        public Person(String name, int age, @StageBuilder.Optional String email) {
                            this.name = name;
                            this.age = age;
                            this.email = email;
                        }
                        
                        public String getName() { return name; }
                        public int getAge() { return age; }
                        public String getEmail() { return email; }
                    }
                    """),
                // Invalid usage - tries to skip required name stage
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
}