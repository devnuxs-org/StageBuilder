package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Comprehensive test demonstrating the from() builder functionality
 * as described in the issue requirements.
 */
public class FromBuilderDemoTest {

    @Test
    public void testFromBuilderWithRecordAsInIssueExample() {
        // This test replicates the exact example from the GitHub issue
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.MyRecord", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public record MyRecord(String a, String b) {}
                    """),
                JavaFileObjects.forSourceString("test.Demo", """
                    package test;
                    
                    public class Demo {
                        public static void demonstrateFromBuilder() {
                            // Create the original object as shown in issue
                            MyRecord someObject = MyRecordStageBuilder.builder()
                                .a("originalA")
                                .b("originalB")
                                .build();
                            
                            // Use the from method as requested in the issue
                            MyRecord newVariant = MyRecordStageBuilder.from(someObject)
                                .b("newValue")
                                .build();
                            
                            // Verify both objects exist and are different
                            System.out.println("Original: " + someObject);
                            System.out.println("New variant: " + newVariant);
                            
                            // Can also modify multiple fields
                            MyRecord anotherVariant = MyRecordStageBuilder.from(someObject)
                                .a("modifiedA")
                                .b("modifiedB")
                                .build();
                            
                            System.out.println("Another variant: " + anotherVariant);
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation).generatedSourceFile("test.MyRecordStageBuilder");
    }

    @Test
    public void testFromBuilderWithComplexClass() {
        // Test from builder with a more complex class example
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.Person", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public class Person {
                        private final String firstName;
                        private final String lastName;
                        private final int age;
                        
                        public Person(String firstName, String lastName, int age) {
                            this.firstName = firstName;
                            this.lastName = lastName;
                            this.age = age;
                        }
                        
                        public String getFirstName() { return firstName; }
                        public String getLastName() { return lastName; }
                        public int getAge() { return age; }
                        
                        @Override
                        public String toString() {
                            return firstName + " " + lastName + " (age " + age + ")";
                        }
                    }
                    """),
                JavaFileObjects.forSourceString("test.PersonDemo", """
                    package test;
                    
                    public class PersonDemo {
                        public static void demonstratePersonFromBuilder() {
                            // Create an original person
                            Person original = PersonStageBuilder.builder()
                                .firstName("John")
                                .lastName("Doe")
                                .age(30)
                                .build();
                            
                            // Create a variant with different age
                            Person older = PersonStageBuilder.from(original)
                                .age(31)
                                .build();
                            
                            // Create a variant with different name
                            Person married = PersonStageBuilder.from(original)
                                .lastName("Smith")
                                .build();
                                
                            System.out.println("Original: " + original);
                            System.out.println("Older: " + older);
                            System.out.println("Married: " + married);
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation).generatedSourceFile("test.PersonStageBuilder");
    }

    @Test
    public void testFromBuilderWithOptionalAndDefaultFields() {
        // Test from builder with optional and default fields
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.User", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public record User(
                        String name,
                        @StageBuilder.Optional String nickname,
                        @StageBuilder.Default("user@example.com") String email
                    ) {}
                    """),
                JavaFileObjects.forSourceString("test.UserDemo", """
                    package test;
                    
                    public class UserDemo {
                        public static void demonstrateUserFromBuilder() {
                            // Create original user with minimal required fields
                            User original = UserStageBuilder.builder()
                                .name("Alice")
                                .build();
                            
                            // Create variant with nickname added
                            User withNickname = UserStageBuilder.from(original)
                                .nickname("Al")
                                .build();
                            
                            // Create variant with custom email
                            User withCustomEmail = UserStageBuilder.from(original)
                                .email("alice@company.com")
                                .build();
                            
                            // Create variant with all fields modified
                            User fullyCustomized = UserStageBuilder.from(original)
                                .name("Alice Smith")
                                .nickname("Ali")
                                .email("alice.smith@company.com")
                                .build();
                                
                            System.out.println("Original: " + original);
                            System.out.println("With nickname: " + withNickname);
                            System.out.println("With custom email: " + withCustomEmail);
                            System.out.println("Fully customized: " + fullyCustomized);
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation).generatedSourceFile("test.UserStageBuilder");
    }
}