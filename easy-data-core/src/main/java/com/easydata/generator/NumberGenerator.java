package com.easydata.generator;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.easydata.GenerationContext;

final class NumberGenerator implements FieldGenerator {

    @Override
    public boolean supports(Class<?> type) {
        return type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == short.class || type == Short.class
                || type == byte.class || type == Byte.class
                || type == double.class || type == Double.class
                || type == float.class || type == Float.class
                || type == BigDecimal.class
                || type == BigInteger.class;
    }

    @Override
    public Object generate(Class<?> type, Type generic, GenerationContext context) {
        if (type == int.class || type == Integer.class) {
            return context.getFaker().number().numberBetween(1, 10000);
        }
        if (type == long.class || type == Long.class) {
            return context.getFaker().number().randomNumber();
        }
        if (type == short.class || type == Short.class) {
            return (short) context.getFaker().number().numberBetween(1, Short.MAX_VALUE);
        }
        if (type == byte.class || type == Byte.class) {
            return (byte) context.getFaker().number().numberBetween(1, 127);
        }
        if (type == double.class || type == Double.class) {
            return context.getFaker().number().randomDouble(2, 0, 10000);
        }
        if (type == float.class || type == Float.class) {
            return (float) context.getFaker().number().randomDouble(2, 0, 10000);
        }
        if (type == BigDecimal.class) {
            return BigDecimal.valueOf(context.getFaker().number().randomDouble(2, 0, 10000));
        }
        if (type == BigInteger.class) {
            return BigInteger.valueOf(context.getFaker().number().randomNumber());
        }
        return 0;
    }
}
