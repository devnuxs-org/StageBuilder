package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * End-to-end demonstration of stage builder functionality.
 */
public class CompleteStageBuilderDemoTest {

    @Test
    public void testCompleteStageBuilderDemo() {
        // Test a complete example with multiple fields
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("demo.User", """
                    package demo;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public class User {
                        private final String firstName;
                        private final String lastName;
                        private final int age;
                        private final String email;
                        
                        public User(String firstName, String lastName, int age, String email) {
                            this.firstName = firstName;
                            this.lastName = lastName;
                            this.age = age;
                            this.email = email;
                        }
                        
                        public String getFirstName() { return firstName; }
                        public String getLastName() { return lastName; }
                        public int getAge() { return age; }
                        public String getEmail() { return email; }
                        
                        @Override
                        public String toString() {
                            return "User{" +
                                   "firstName='" + firstName + "'" +
                                   ", lastName='" + lastName + "'" +
                                   ", age=" + age +
                                   ", email='" + email + "'" +
                                   '}';
                        }
                    }
                    """),
                JavaFileObjects.forSourceString("demo.Product", """
                    package demo;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public record Product(String name, double price, String category) {
                    }
                    """),
                JavaFileObjects.forSourceString("demo.Demo", """
                    package demo;
                    
                    public class Demo {
                        public static void main(String[] args) {
                            // Create a user using the stage builder
                            User user = UserStageBuilder.builder()
                                .firstName("John")
                                .lastName("Doe")
                                .age(30)
                                .email("john.doe@example.com")
                                .build();
                            
                            System.out.println("Created user: " + user);
                            
                            // Create a product using the stage builder
                            Product product = ProductStageBuilder.builder()
                                .name("Laptop")
                                .price(999.99)
                                .category("Electronics")
                                .build();
                            
                            System.out.println("Created product: " + product);
                            
                            // Demonstrate stage ordering enforcement
                            User user2 = UserStageBuilder.builder()
                                .firstName("Alice")
                                .lastName("Smith")
                                .age(25)
                                .email("alice.smith@example.com")
                                .build();
                            
                            System.out.println("Created user2: " + user2);
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder classes exist
        assertThat(compilation).generatedSourceFile("demo.UserStageBuilder");
        assertThat(compilation).generatedSourceFile("demo.ProductStageBuilder");
    }

    @Test
    public void testStageBuilderWithNonPublicClasses() {
        // Test that stage builders work with package-private classes
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("demo.InternalClass", """
                    package demo;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    class InternalClass {
                        private final String name;
                        private final int value;
                        
                        public InternalClass(String name, int value) {
                            this.name = name;
                            this.value = value;
                        }
                        
                        public String getName() { return name; }
                        public int getValue() { return value; }
                    }
                    """),
                JavaFileObjects.forSourceString("demo.InternalUsage", """
                    package demo;
                    
                    public class InternalUsage {
                        public static InternalClass createInternal() {
                            return InternalClassStageBuilder.builder()
                                .name("test")
                                .value(42)
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation).generatedSourceFile("demo.InternalClassStageBuilder");
    }

    @Test
    public void testStageBuilderWithDifferentFieldTypes() {
        // Test that stage builders work with different field types
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("demo.ComplexClass", """
                    package demo;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    import java.util.List;
                    
                    @StageBuilder
                    public class ComplexClass {
                        private final String stringField;
                        private final int intField;
                        private final double doubleField;
                        private final boolean booleanField;
                        private final List<String> listField;
                        
                        public ComplexClass(String stringField, int intField, double doubleField, boolean booleanField, List<String> listField) {
                            this.stringField = stringField;
                            this.intField = intField;
                            this.doubleField = doubleField;
                            this.booleanField = booleanField;
                            this.listField = listField;
                        }
                        
                        public String getStringField() { return stringField; }
                        public int getIntField() { return intField; }
                        public double getDoubleField() { return doubleField; }
                        public boolean getBooleanField() { return booleanField; }
                        public List<String> getListField() { return listField; }
                    }
                    """),
                JavaFileObjects.forSourceString("demo.ComplexUsage", """
                    package demo;
                    
                    import java.util.Arrays;
                    
                    public class ComplexUsage {
                        public static ComplexClass createComplex() {
                            return ComplexClassStageBuilder.builder()
                                .stringField("test")
                                .intField(42)
                                .doubleField(3.14)
                                .booleanField(true)
                                .listField(Arrays.asList("a", "b", "c"))
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation).generatedSourceFile("demo.ComplexClassStageBuilder");
    }
}