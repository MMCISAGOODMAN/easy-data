package com.easydata.generator;

import java.lang.reflect.Type;

import com.easydata.GenerationContext;

final class StringGenerator implements FieldGenerator {

    @Override
    public boolean supports(Class<?> type) {
        return type == String.class || type == Character.class || type == char.class;
    }

    @Override
    public Object generate(Class<?> type, Type generic, GenerationContext context) {
        if (type == char.class || type == Character.class) {
            return context.getFaker().lorem().character();
        }
        return context.getFaker().lorem().word();
    }
}
