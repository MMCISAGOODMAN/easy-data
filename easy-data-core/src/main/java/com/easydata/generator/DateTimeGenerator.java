package com.easydata.generator;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import com.easydata.GenerationContext;

/**
 * Supports legacy {@link Date} and JDK 8 time types.
 */
final class DateTimeGenerator implements FieldGenerator {

    @Override
    public boolean supports(Class<?> type) {
        return type == Date.class
                || type == LocalDate.class
                || type == LocalDateTime.class
                || type == LocalTime.class
                || type == Instant.class;
    }

    @Override
    public Object generate(Class<?> type, Type generic, GenerationContext context) {
        if (type == Date.class) {
            return context.getFaker().date().birthday();
        }
        if (type == LocalDate.class) {
            Date date = context.getFaker().date().birthday();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (type == LocalDateTime.class) {
            Date date = context.getFaker().date().birthday();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        if (type == LocalTime.class) {
            return LocalTime.now().withNano(0);
        }
        if (type == Instant.class) {
            return context.getFaker().date().birthday().toInstant();
        }
        return null;
    }
}
