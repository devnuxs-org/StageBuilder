package org.devnuxs.stagebuilder.processor;

import com.google.auto.service.AutoService;
import org.devnuxs.stagebuilder.api.StageBuilder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * Annotation processor for the {@link StageBuilder} annotation.
 * 
 * <p>This processor recognizes classes and records annotated with {@code @StageBuilder}
 * and will generate fluent builder classes for them.</p>
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("org.devnuxs.stagebuilder.api.StageBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class StageBuilderProcessor extends AbstractProcessor {
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Get all elements annotated with @StageBuilder
        var annotatedElements = roundEnv.getElementsAnnotatedWith(StageBuilder.class);
        
        if (!annotatedElements.isEmpty()) {
            // Log that we found annotations to process
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE, 
                "Found " + annotatedElements.size() + " element(s) annotated with @StageBuilder"
            );
            
            // For each annotated element, log its name
            for (var element : annotatedElements) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "Processing @StageBuilder annotation on: " + element.getSimpleName()
                );
            }
        }
        
        // Return true to indicate we've handled this annotation
        return true;
    }
}