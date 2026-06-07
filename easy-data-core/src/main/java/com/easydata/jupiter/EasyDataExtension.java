package com.easydata.jupiter;

import java.lang.reflect.Parameter;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.easydata.EasyData;
import com.easydata.annotation.GenerateData;

/**
 * JUnit 5 extension that injects auto-generated test data into method parameters
 * annotated with or declared under {@link GenerateData}.
 */
public class EasyDataExtension implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return isGenerateDataParameter(parameterContext);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        try {
            return EasyData.generate(type);
        } catch (RuntimeException e) {
            throw new ParameterResolutionException(
                    "Failed to generate test data for parameter type: " + type.getName(), e);
        }
    }

    private static boolean isGenerateDataParameter(ParameterContext parameterContext) {
        Parameter parameter = parameterContext.getParameter();
        if (parameter.isAnnotationPresent(GenerateData.class)) {
            return true;
        }
        Optional<GenerateData> onMethod = parameterContext.getDeclaringExecutable()
                .getAnnotation(GenerateData.class) == null
                ? Optional.empty()
                : Optional.of(parameterContext.getDeclaringExecutable().getAnnotation(GenerateData.class));
        return onMethod.isPresent() && !parameter.getType().isPrimitive();
    }
}
