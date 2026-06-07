package com.easydata.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.easydata.annotation.EasyData;

/**
 * Compile-time validator for {@link EasyData}, {@link EasyData.Ignore}, and {@link EasyData.Field}.
 */
@SupportedAnnotationTypes({
        "com.easydata.annotation.EasyData",
        "com.easydata.annotation.EasyData.Ignore",
        "com.easydata.annotation.EasyData.Field"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class EasyDataProcessor extends AbstractProcessor {

    private EasyDataValidator validator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.validator = new EasyDataValidator(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(EasyData.class)) {
            if (element instanceof TypeElement) {
                validator.validateEasyDataType((TypeElement) element);
            } else {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "@EasyData can only be applied to classes",
                        element);
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(EasyData.Ignore.class)) {
            validator.validateIgnoreField(element);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(EasyData.Field.class)) {
            validator.validateFixedField(element);
        }

        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        SourceVersion[] versions = SourceVersion.values();
        return versions[versions.length - 1];
    }
}
