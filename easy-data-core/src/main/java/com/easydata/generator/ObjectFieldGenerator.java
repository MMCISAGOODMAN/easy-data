package com.easydata.generator;

import java.lang.reflect.Type;

import com.easydata.DataGenerator;
import com.easydata.GenerationContext;

/**
 * Fallback generator for POJO / nested object types.
 */
final class ObjectFieldGenerator implements FieldGenerator {

    private final DataGenerator dataGenerator;

    ObjectFieldGenerator(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public boolean supports(Class<?> type) {
        return !type.isPrimitive()
                && !type.isEnum()
                && !type.isArray()
                && !CharSequence.class.isAssignableFrom(type)
                && !Number.class.isAssignableFrom(type)
                && !Boolean.class.isAssignableFrom(type)
                && type != Object.class;
    }

    @Override
    public Object generate(Class<?> type, Type generic, GenerationContext context) {
        return dataGenerator.generate(type, context);
    }
}
