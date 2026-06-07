package com.easydata;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.easydata.annotation.EasyData;
import com.easydata.exception.GenerationException;

/**
 * Parses {@link EasyData.Field} annotation values into typed field values.
 */
final class AnnotationFieldValues {

    private AnnotationFieldValues() {
    }

    static boolean hasFixedValue(Field field) {
        return field.isAnnotationPresent(EasyData.Field.class);
    }

    static Object parseFixedValue(Field field) {
        EasyData.Field annotation = field.getAnnotation(EasyData.Field.class);
        if (annotation == null) {
            return null;
        }
        return parse(annotation.value(), field.getType());
    }

    static Object parse(String raw, Class<?> targetType) {
        if (targetType == String.class) {
            return raw;
        }
        if (targetType == char.class || targetType == Character.class) {
            if (raw.length() != 1) {
                throw new GenerationException("Char @EasyData.Field value must be a single character: " + raw);
            }
            return raw.charAt(0);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(raw);
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(raw);
        }
        if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(raw);
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(raw);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(raw);
        }
        if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(raw);
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(raw);
        }
        if (targetType == BigDecimal.class) {
            return new BigDecimal(raw);
        }
        if (targetType == BigInteger.class) {
            return new BigInteger(raw);
        }
        if (targetType.isEnum()) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object value = Enum.valueOf((Class<Enum>) targetType, raw);
            return value;
        }
        throw new GenerationException("Unsupported @EasyData.Field type: " + targetType.getName());
    }
}
