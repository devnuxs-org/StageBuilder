package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Test for stage builder with field-level annotations and no-args constructor using direct field access.
 */
class FieldAnnotationNoArgsDirectAccessTest {
    @Test
    void testFieldAnnotationsWithNoArgsConstructorAndDirectAccess() {
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.User", """
                    package test;
                    import org.devnuxs.stagebuilder.api.StageBuilder;

                    @StageBuilder
                    public class User {
                        @StageBuilder.Optional
                        public String name;
                        @StageBuilder.Default("42")
                        public int age;
                        public String email;

                        public User() {}
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
                                .age(99)
                                .name("Bob")
                                .build();
                        }
                    }
                    """));
        assertThat(compilation).succeededWithoutWarnings();
        assertThat(compilation).generatedSourceFile("test.UserStageBuilder");
    }
}
