package com.easydata;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easydata.annotation.EasyData;
import com.easydata.exception.GenerationException;
import com.easydata.generator.FieldGeneratorRegistry;

/**
 * Core engine that recursively populates object graphs via reflection.
 */
public class DataGenerator {

    private static final Logger log = LoggerFactory.getLogger(DataGenerator.class);

    private final FieldGeneratorRegistry registry;

    public DataGenerator() {
        registry = FieldGeneratorRegistry.createFor(this);
    }

    /**
     * Generates and fully populates an instance of {@code type}.
     */
    public <T> T generate(Class<T> type) {
        return generate(type, GenerationContext.builder().build());
    }

    /**
     * Generates an instance using the supplied context (custom fields, sizes, ignores).
     */
    @SuppressWarnings("unchecked")
    public <T> T generate(Class<T> type, GenerationContext context) {
        if (type == null) {
            throw new GenerationException("Type must not be null");
        }
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            throw new GenerationException("Cannot instantiate abstract type or interface: " + type.getName());
        }

        if (context.isGenerating(type)) {
            log.debug("Breaking circular type reference for {}", type.getSimpleName());
            return instantiate(type);
        }

        context.pushGenerating(type);
        try {
            T instance = instantiate(type);
            populate(instance, "", context);
            return instance;
        } finally {
            context.popGenerating(type);
        }
    }

    /**
     * Recursively walks declared fields and assigns generated values.
     * Nested paths use dot notation: {@code order.user.address.city}.
     */
    void populate(Object instance, String pathPrefix, GenerationContext context) {
        if (instance == null) {
            return;
        }

        Class<?> clazz = instance.getClass();
        if (context.isVisiting(instance)) {
            log.debug("Skipping instance cycle at {}", clazz.getSimpleName());
            return;
        }

        if (!context.beginVisit(instance)) {
            return;
        }

        try {
            for (Field field : getAllFields(clazz)) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }

                String fieldPath = pathPrefix.isEmpty() ? field.getName() : pathPrefix + "." + field.getName();

                if (context.isIgnored(fieldPath) || field.isAnnotationPresent(EasyData.Ignore.class)) {
                    log.debug("Ignoring field {}", fieldPath);
                    continue;
                }

                if (context.hasCustomValue(fieldPath)) {
                    setFieldValue(instance, field, context.getCustomValue(fieldPath));
                    continue;
                }

                if (AnnotationFieldValues.hasFixedValue(field)) {
                    setFieldValue(instance, field, AnnotationFieldValues.parseFixedValue(field));
                    continue;
                }

                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Type genericType = field.getGenericType();
                GenerationContext fieldContext = context.nested(field.getName());

                Object value = registry.generateValue(fieldType, genericType, fieldContext);
                setFieldValue(instance, field, value);
            }
        } finally {
            context.endVisit(instance);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiate(Class<T> type) {
        try {
            Constructor<T> ctor = findNoArgConstructor(type);
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new GenerationException("Failed to instantiate " + type.getName()
                    + ". Ensure a public no-arg constructor exists.", e);
        }
    }

    private static <T> Constructor<T> findNoArgConstructor(Class<T> type) throws NoSuchMethodException {
        try {
            return type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            return type.getConstructor();
        }
    }

    private static void setFieldValue(Object instance, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new GenerationException("Cannot set field " + field.getName() + " on "
                    + instance.getClass().getName(), e);
        }
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                fields.add(field);
            }
            current = current.getSuperclass();
        }
        return fields;
    }
}
