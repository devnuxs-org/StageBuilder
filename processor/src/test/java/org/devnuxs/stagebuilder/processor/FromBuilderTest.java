package org.devnuxs.stagebuilder.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Tests for the from() builder method functionality.
 */
public class FromBuilderTest {

    @Test
    public void testFromMethodWithRecord() {
        // Test the from method with a record
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.MyRecord", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public record MyRecord(String a, String b) {}
                    """),
                JavaFileObjects.forSourceString("test.FromRecordUsage", """
                    package test;
                    
                    public class FromRecordUsage {
                        public static void testFromMethod() {
                            // Create original object
                            MyRecord original = MyRecordStageBuilder.builder()
                                .a("originalA")
                                .b("originalB")
                                .build();
                            
                            // Create new object from existing one with modified field
                            MyRecord modified = MyRecordStageBuilder.from(original)
                                .b("newValue")
                                .build();
                                
                            System.out.println("Original: " + original);
                            System.out.println("Modified: " + modified);
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation).generatedSourceFile("test.MyRecordStageBuilder");
    }

    @Test
    public void testFromMethodWithClass() {
        // Test the from method with a regular class
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.MyClass", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public class MyClass {
                        private final String a;
                        private final String b;
                        
                        public MyClass(String a, String b) {
                            this.a = a;
                            this.b = b;
                        }
                        
                        public String getA() { return a; }
                        public String getB() { return b; }
                    }
                    """),
                JavaFileObjects.forSourceString("test.FromClassUsage", """
                    package test;
                    
                    public class FromClassUsage {
                        public static void testFromMethod() {
                            // Create original object
                            MyClass original = MyClassStageBuilder.builder()
                                .a("originalA")
                                .b("originalB")
                                .build();
                            
                            // Create new object from existing one with modified field
                            MyClass modified = MyClassStageBuilder.from(original)
                                .b("newValue")
                                .build();
                                
                            System.out.println("Original A: " + original.getA() + ", B: " + original.getB());
                            System.out.println("Modified A: " + modified.getA() + ", B: " + modified.getB());
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
        
        // Verify the generated stage builder class exists
        assertThat(compilation).generatedSourceFile("test.MyClassStageBuilder");
    }

    @Test
    public void testFromMethodWithOptionalFields() {
        // Test the from method with optional fields
        var compilation = javac()
            .withProcessors(new StageBuilderProcessor())
            .compile(
                JavaFileObjects.forSourceString("test.OptionalRecord", """
                    package test;
                    
                    import org.devnuxs.stagebuilder.api.StageBuilder;
                    
                    @StageBuilder
                    public record OptionalRecord(String required, @StageBuilder.Optional String optional) {}
                    """),
                JavaFileObjects.forSourceString("test.FromOptionalUsage", """
                    package test;
                    
                    public class FromOptionalUsage {
                        public static void testFromMethod() {
                            // Create original object with optional field
                            OptionalRecord original = OptionalRecordStageBuilder.builder()
                                .required("req")
                                .optional("opt")
                                .build();
                            
                            // Create new object from existing one
                            OptionalRecord modified = OptionalRecordStageBuilder.from(original)
                                .optional("newOpt")
                                .build();
                        }
                    }
                    """));
        
        // Verify compilation succeeds
        assertThat(compilation).succeededWithoutWarnings();
    }
}