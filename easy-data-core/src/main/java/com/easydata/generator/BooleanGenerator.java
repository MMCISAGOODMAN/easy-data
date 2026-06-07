package com.easydata.generator;

import java.lang.reflect.Type;

import com.easydata.GenerationContext;

final class BooleanGenerator implements FieldGenerator {

    @Override
    public boolean supports(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    @Override
    public Object generate(Class<?> type, Type generic, GenerationContext context) {
        return context.getFaker().bool().bool();
    }
}
