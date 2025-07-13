package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import java.util.Locale;

/**
 * Tests for inner class and record functionality with annotations in stage builders.
 */
public class InnerClassRecordAnnotationTest {

    @BeforeAll
    public static void setup() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void testStaticInnerClassWithStageBuilder() {
        // Test that inner classes with @StageBuilder work correctly
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.OuterClass", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    public class OuterClass {
                        
                        @StageBuilder
                        public static class InnerPerson {
                            private final String name;
                            private final int age;
                            
                            public InnerPerson(String name, int age) {
                                this.name = name;
                                this.age = age;
                            }
                            
                            public String getName() { return name; }
                            public int getAge() { return age; }
                        }
                    }
                    """),
                // Valid usage - using the generated stage builder for inner class
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static OuterClass.InnerPerson createPerson() {
                            return InnerPersonStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation)
            .generatedSourceFile("test.InnerPersonStageBuilder");
    }

    @Test
    public void testStaticInnerRecordWithStageBuilder() {
        // Test that inner records with @StageBuilder work correctly
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.OuterClass", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    public class OuterClass {
                        
                        @StageBuilder
                        public static record InnerPersonRecord(String name, int age, String email) {
                        }
                    }
                    """),
                // Valid usage - using the generated stage builder for inner record
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static OuterClass.InnerPersonRecord createPerson() {
                            return InnerPersonRecordStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .email("john@example.com")
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation)
            .generatedSourceFile("test.InnerPersonRecordStageBuilder");
    }

    @Test
    public void testInnerClassWithOptionalAnnotation() {
        // Test that inner classes with @StageBuilder.Optional work correctly
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.OuterClass", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    public class OuterClass {
                        
                        @StageBuilder
                        public static class InnerPerson {
                            private final String name;
                            private final int age;
                            private final String email;
                            
                            public InnerPerson(String name, int age, @StageBuilder.Optional String email) {
                                this.name = name;
                                this.age = age;
                                this.email = email;
                            }
                            
                            public String getName() { return name; }
                            public int getAge() { return age; }
                            public String getEmail() { return email; }
                        }
                    }
                    """),
                // Valid usage - builds without setting optional field
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static OuterClass.InnerPerson createPersonWithoutEmail() {
                            return InnerPersonStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .build();
                        }
                        
                        public static OuterClass.InnerPerson createPersonWithEmail() {
                            return InnerPersonStageBuilder.builder()
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
    public void testInnerRecordWithOptionalAnnotation() {
        // Test that inner records with @StageBuilder.Optional work correctly
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.OuterClass", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    public class OuterClass {
                        
                        @StageBuilder
                        public static record InnerPersonRecord(String name, int age, @StageBuilder.Optional String email) {
                        }
                    }
                    """),
                // Valid usage - builds without setting optional field
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static OuterClass.InnerPersonRecord createPersonWithoutEmail() {
                            return InnerPersonRecordStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .build();
                        }
                        
                        public static OuterClass.InnerPersonRecord createPersonWithEmail() {
                            return InnerPersonRecordStageBuilder.builder()
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
    public void testInnerClassWithDefaultAnnotation() {
        // Test that inner classes with @StageBuilder.Default work correctly
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.OuterClass", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    public class OuterClass {
                        
                        @StageBuilder
                        public static class InnerPerson {
                            private final String name;
                            private final int age;
                            private final String email;
                            
                            public InnerPerson(String name, int age, @StageBuilder.Default(value = \"default@email.com\") String email) {
                                this.name = name;
                                this.age = age;
                                this.email = email;
                            }
                            
                            public String getName() { return name; }
                            public int getAge() { return age; }
                            public String getEmail() { return email; }
                        }
                    }
                    """),
                // Valid usage - builds without setting default field
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static OuterClass.InnerPerson createPersonWithoutEmail() {
                            return InnerPersonStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .build();
                        }
                        
                        public static OuterClass.InnerPerson createPersonWithEmail() {
                            return InnerPersonStageBuilder.builder()
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
    public void testInnerRecordWithDefaultAnnotation() {
        // Test that inner records with @StageBuilder.Default work correctly
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.OuterClass", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    public class OuterClass {
                        
                        @StageBuilder
                        public static record InnerPersonRecord(String name, int age, @StageBuilder.Default(value = \"default@email.com\") String email) {
                        }
                    }
                    """),
                // Valid usage - builds without setting default field
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static OuterClass.InnerPersonRecord createPersonWithoutEmail() {
                            return InnerPersonRecordStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .build();
                        }
                        
                        public static OuterClass.InnerPersonRecord createPersonWithEmail() {
                            return InnerPersonRecordStageBuilder.builder()
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
    public void testMultipleInnerClassesWithStageBuilder() {
        // Test multiple inner classes with @StageBuilder in the same outer class
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.OuterClass", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    public class OuterClass {
                        
                        @StageBuilder
                        public static class InnerPerson {
                            private final String name;
                            private final int age;
                            
                            public InnerPerson(String name, int age) {
                                this.name = name;
                                this.age = age;
                            }
                            
                            public String getName() { return name; }
                            public int getAge() { return age; }
                        }
                        
                        @StageBuilder
                        public static class InnerCompany {
                            private final String companyName;
                            private final String address;
                            
                            public InnerCompany(String companyName, String address) {
                                this.companyName = companyName;
                                this.address = address;
                            }
                            
                            public String getCompanyName() { return companyName; }
                            public String getAddress() { return address; }
                        }
                    }
                    """),
                // Valid usage - using both generated stage builders
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static OuterClass.InnerPerson createPerson() {
                            return InnerPersonStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .build();
                        }
                        
                        public static OuterClass.InnerCompany createCompany() {
                            return InnerCompanyStageBuilder.builder()
                                .companyName("Tech Corp")
                                .address("123 Main St")
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify both generated stage builder classes exist
        assertThat(compilation)
            .generatedSourceFile("test.InnerPersonStageBuilder");
        assertThat(compilation)
            .generatedSourceFile("test.InnerCompanyStageBuilder");
    }

    @Test
    public void testNestedInnerClassWithStageBuilder() {
        // Test nested inner classes with @StageBuilder
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.OuterClass", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    public class OuterClass {
                        
                        public static class MiddleClass {
                            
                            @StageBuilder
                            public static class DeeplyNestedPerson {
                                private final String name;
                                private final String role;
                                
                                public DeeplyNestedPerson(String name, String role) {
                                    this.name = name;
                                    this.role = role;
                                }
                                
                                public String getName() { return name; }
                                public String getRole() { return role; }
                            }
                        }
                    }
                    """),
                // Valid usage - using the deeply nested generated stage builder
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static OuterClass.MiddleClass.DeeplyNestedPerson createPerson() {
                            return DeeplyNestedPersonStageBuilder.builder()
                                .name("Alice")
                                .role("Developer")
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation)
            .generatedSourceFile("test.DeeplyNestedPersonStageBuilder");
    }

    @Test
    public void testInnerClassWithMixedAnnotations() {
        // Test inner class with mixed @StageBuilder.Optional and @StageBuilder.Default annotations
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.OuterClass", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    public class OuterClass {
                        
                        @StageBuilder
                        public static class InnerPerson {
                            private final String name;
                            private final int age;
                            private final String email;
                            private final String phone;
                            
                            public InnerPerson(String name, int age, 
                                               @StageBuilder.Default(value = \"default@email.com\") String email,
                                               @StageBuilder.Optional String phone) {
                                this.name = name;
                                this.age = age;
                                this.email = email;
                                this.phone = phone;
                            }
                            
                            public String getName() { return name; }
                            public int getAge() { return age; }
                            public String getEmail() { return email; }
                            public String getPhone() { return phone; }
                        }
                    }
                    """),
                // Valid usage - various combinations
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static OuterClass.InnerPerson createPersonMinimal() {
                            return InnerPersonStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .build();
                        }
                        
                        public static OuterClass.InnerPerson createPersonWithPhone() {
                            return InnerPersonStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .phone("555-1234")
                                .build();
                        }
                        
                        public static OuterClass.InnerPerson createPersonWithCustomEmail() {
                            return InnerPersonStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .email("custom@email.com")
                                .phone("555-1234")
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void testInnerRecordWithMixedAnnotations() {
        // Test inner record with mixed @StageBuilder.Optional and @StageBuilder.Default annotations
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.OuterClass", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    public class OuterClass {
                        
                        @StageBuilder
                        public static record InnerPersonRecord(String name, 
                                                               int age, 
                                                               @StageBuilder.Default(value = \"default@email.com\") String email,
                                                               @StageBuilder.Optional String phone) {
                        }
                    }
                    """),
                // Valid usage - various combinations
                JavaFileObjects.forSourceString("test.ValidUsage", """
                    package test;
                    
                    public class ValidUsage {
                        public static OuterClass.InnerPersonRecord createPersonMinimal() {
                            return InnerPersonRecordStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .build();
                        }
                        
                        public static OuterClass.InnerPersonRecord createPersonWithPhone() {
                            return InnerPersonRecordStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .phone("555-1234")
                                .build();
                        }
                        
                        public static OuterClass.InnerPersonRecord createPersonWithCustomEmail() {
                            return InnerPersonRecordStageBuilder.builder()
                                .name("John")
                                .age(30)
                                .email("custom@email.com")
                                .phone("555-1234")
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }
}