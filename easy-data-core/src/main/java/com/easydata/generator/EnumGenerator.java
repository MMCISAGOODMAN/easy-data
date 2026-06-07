package com.easydata.generator;

import java.lang.reflect.Type;

import com.easydata.GenerationContext;

final class EnumGenerator implements FieldGenerator {

    @Override
    public boolean supports(Class<?> type) {
        return type.isEnum();
    }

    @Override
    public Object generate(Class<?> type, Type generic, GenerationContext context) {
        Object[] constants = type.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return null;
        }
        int index = context.getFaker().number().numberBetween(0, constants.length);
        return constants[index];
    }
}
