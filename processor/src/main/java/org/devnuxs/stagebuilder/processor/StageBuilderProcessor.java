package org.devnuxs.stagebuilder.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.devnuxs.stagebuilder.api.StageBuilder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Annotation processor for the {@link StageBuilder} annotation.
 * 
 * <p>This processor recognizes classes and records annotated with {@code @StageBuilder}
 * and will generate fluent builder classes for them.</p>
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"org.devnuxs.stagebuilder.api.StageBuilder", "org.devnuxs.stagebuilder.api.StageBuilder.Optional"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class StageBuilderProcessor extends AbstractProcessor {
    
    private final FieldExtractor fieldExtractor = new FieldExtractor();
    private final StageInterfaceGenerator stageInterfaceGenerator = new StageInterfaceGenerator();
    private final BuilderClassGenerator builderClassGenerator = new BuilderClassGenerator();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Get all elements annotated with @StageBuilder
        var annotatedElements = roundEnv.getElementsAnnotatedWith(StageBuilder.class);
        
        for (var element : annotatedElements) {
            if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.RECORD) {
                try {
                    generateStageBuilder((TypeElement) element);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Failed to generate stage builder for " + element.getSimpleName() + ": " + e.getMessage(),
                        element
                    );
                }
            }
        }
        
        return true;
    }
    
    private void generateStageBuilder(TypeElement element) throws IOException {
        String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        String className = element.getSimpleName().toString();
        String builderClassName = className + "StageBuilder";
        
        List<FieldInfo> fields = fieldExtractor.extractFields(element);
        
        if (fields.isEmpty()) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "No fields found for stage builder generation in " + className,
                element
            );
            return;
        }
        
        TypeSpec builderClass = createBuilderClass(builderClassName, fields, className, packageName, element);
        
        JavaFile javaFile = JavaFile.builder(packageName, builderClass).build();
        javaFile.writeTo(processingEnv.getFiler());
    }
    
    private TypeSpec createBuilderClass(String builderClassName, List<FieldInfo> fields, 
                                       String className, String packageName, TypeElement element) {
        TypeSpec.Builder builderClass = TypeSpec.classBuilder(builderClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        
        List<TypeSpec> stageInterfaces = stageInterfaceGenerator.generateStageInterfaces(fields, className);
        builderClass.addTypes(stageInterfaces);
        
        MethodSpec builderMethod = createBuilderMethod(fields, packageName, builderClassName);
        builderClass.addMethod(builderMethod);
        
        TypeSpec builderInnerClass = builderClassGenerator.generateBuilderInnerClass(fields, className, element);
        builderClass.addType(builderInnerClass);
        
        return builderClass.build();
    }
    
    private MethodSpec createBuilderMethod(List<FieldInfo> fields, String packageName, String builderClassName) {
        return MethodSpec.methodBuilder("builder")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(ClassName.get(packageName, builderClassName, 
                CodeGenerationUtils.getFirstStageInterfaceName(fields)))
            .addStatement("return new Builder()")
            .build();
    }
}