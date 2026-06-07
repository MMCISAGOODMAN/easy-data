package com.easydata.generator;

import java.lang.reflect.Type;

import com.easydata.GenerationContext;

/**
 * Strategy for generating values of a specific field type.
 */
public interface FieldGenerator {

    /**
     * @return {@code true} if this generator can handle the given raw type
     */
    boolean supports(Class<?> type);

    /**
     * Generates a value for the given type.
     *
     * @param type    raw field type
     * @param generic generic type (may be {@code null})
     * @param context generation context
     * @return generated value, never {@code null} for non-nullable primitives (boxed)
     */
    Object generate(Class<?> type, Type generic, GenerationContext context);
}
