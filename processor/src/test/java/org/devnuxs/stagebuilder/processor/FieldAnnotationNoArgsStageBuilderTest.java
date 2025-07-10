package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Test for stage builder with field-level annotations and no-args constructor.
 */
class FieldAnnotationNoArgsStageBuilderTest {
    @Test
    void testFieldAnnotationsWithNoArgsConstructor() {
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.User", """
                    package test;
                    import org.devnuxs.stagebuilder.api.StageBuilder;

                    @StageBuilder
                    public class User {
                        @StageBuilder.Optional
                        private String name;
                        @StageBuilder.Default("42")
                        private int age;
                        private String email;

                        public User() {}

                        public void setName(String name) { this.name = name; }
                        public void setAge(int age) { this.age = age; }
                        public void setEmail(String email) { this.email = email; }
                        public String getName() { return name; }
                        public int getAge() { return age; }
                        public String getEmail() { return email; }
                    }
                    """),
                JavaFileObjects.forSourceString("test.Usage", """
                    package test;
                    public class Usage {
                        public static User createUser() {
                            return UserStageBuilder.builder()
                                .email("foo@bar.com")
                                .build();
                        }
                        public static User createUserWithName() {
                            return UserStageBuilder.builder()
                                .email("alice@bar.com")
                                .name("Alice")
                                .build();
                        }
                        public static User createUserWithAll() {
                            return UserStageBuilder.builder()
                                .email("bob@bar.com")
                                .name("Bob")
                                .age(99)
                                .build();
                        }
                    }
                    """));
        assertThat(compilation).succeededWithoutWarnings();
        assertThat(compilation).generatedSourceFile("test.UserStageBuilder");
    }
}
