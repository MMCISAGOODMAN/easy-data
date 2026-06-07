package com.easydata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.easydata.annotation.EasyData;

/**
 * Fluent builder for customizing test data generation.
 */
public final class EasyDataBuilder<T> {

    private final Class<T> type;
    private final GenerationContext.Builder contextBuilder;
    private final DataGenerator generator;

    EasyDataBuilder(Class<T> type, DataGenerator generator) {
        this.type = type;
        this.generator = generator;
        this.contextBuilder = GenerationContext.builder();

        EasyData annotation = type.getAnnotation(EasyData.class);
        if (annotation != null && annotation.defaultSize() > 0) {
            contextBuilder.defaultCollectionSize(annotation.defaultSize());
        }
    }

    /**
     * Sets a fixed value for a field (supports dot paths: {@code "address.city"}).
     */
    public EasyDataBuilder<T> field(String name, Object value) {
        contextBuilder.field(name, value);
        return this;
    }

    /**
     * Sets collection/map size for a named field.
     */
    public EasyDataBuilder<T> size(String name, int count) {
        contextBuilder.size(name, count);
        return this;
    }

    /**
     * Skips a field during generation.
     */
    public EasyDataBuilder<T> ignore(String name) {
        contextBuilder.ignore(name);
        return this;
    }

    /**
     * Generates a single populated instance.
     */
    public T generate() {
        return generator.generate(type, contextBuilder.build());
    }

    /**
     * Generates multiple populated instances.
     */
    public List<T> generate(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        if (count == 0) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<T>(count);
        for (int i = 0; i < count; i++) {
            result.add(generate());
        }
        return result;
    }
}
