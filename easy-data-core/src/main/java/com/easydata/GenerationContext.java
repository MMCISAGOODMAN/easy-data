package com.easydata;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import com.github.javafaker.Faker;

/**
 * Holds per-generation state: custom values, sizes, ignored fields, and cycle detection.
 */
public class GenerationContext {

    private static final int DEFAULT_COLLECTION_SIZE = 3;

    private final Faker faker;
    private final Map<String, Object> customFields;
    private final Map<String, Integer> collectionSizes;
    private final Set<String> ignoredFields;
    private final Set<Object> visiting;
    private final Set<Class<?>> generatingTypes;
    private final int defaultCollectionSize;

    private GenerationContext(Builder builder) {
        this.faker = builder.faker != null ? builder.faker : new Faker();
        this.customFields = new HashMap<String, Object>(builder.customFields);
        this.collectionSizes = new HashMap<String, Integer>(builder.collectionSizes);
        this.ignoredFields = new HashSet<String>(builder.ignoredFields);
        this.visiting = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
        this.generatingTypes = new HashSet<Class<?>>(builder.generatingTypes);
        this.defaultCollectionSize = builder.defaultCollectionSize > 0
                ? builder.defaultCollectionSize
                : DEFAULT_COLLECTION_SIZE;
    }

    public Faker getFaker() {
        return faker;
    }

    public int getCollectionSize(String fieldPath) {
        Integer size = collectionSizes.get(fieldPath);
        return size != null ? size : defaultCollectionSize;
    }

    public int getDefaultCollectionSize() {
        return defaultCollectionSize;
    }

    public boolean isIgnored(String fieldPath) {
        return ignoredFields.contains(fieldPath);
    }

    public Object getCustomValue(String fieldPath) {
        return customFields.containsKey(fieldPath) ? customFields.get(fieldPath) : null;
    }

    public boolean hasCustomValue(String fieldPath) {
        return customFields.containsKey(fieldPath);
    }

    public boolean isVisiting(Object instance) {
        return visiting.contains(instance);
    }

    public boolean beginVisit(Object instance) {
        return visiting.add(instance);
    }

    public void endVisit(Object instance) {
        visiting.remove(instance);
    }

    /**
     * Detects type-level circular references (A → B → A).
     */
    public boolean isGenerating(Class<?> type) {
        return generatingTypes.contains(type);
    }

    public void pushGenerating(Class<?> type) {
        generatingTypes.add(type);
    }

    public void popGenerating(Class<?> type) {
        generatingTypes.remove(type);
    }

    /**
     * Creates a nested context inheriting faker, sizes, and prefixed overrides.
     */
    public GenerationContext nested(String fieldName) {
        String prefix = fieldName + ".";
        Builder builder = new Builder()
                .faker(faker)
                .defaultCollectionSize(defaultCollectionSize)
                .generatingTypes(generatingTypes);

        for (Map.Entry<String, Object> entry : customFields.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                builder.field(entry.getKey().substring(prefix.length()), entry.getValue());
            }
        }
        for (Map.Entry<String, Integer> entry : collectionSizes.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                builder.size(entry.getKey().substring(prefix.length()), entry.getValue());
            } else if (entry.getKey().equals(fieldName)) {
                builder.defaultCollectionSize(entry.getValue());
            }
        }
        for (String ignored : ignoredFields) {
            if (ignored.startsWith(prefix)) {
                builder.ignore(ignored.substring(prefix.length()));
            } else if (ignored.equals(fieldName)) {
                builder.ignore("");
            }
        }
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(GenerationContext source) {
        return new Builder()
                .faker(source.faker)
                .defaultCollectionSize(source.defaultCollectionSize)
                .generatingTypes(source.generatingTypes)
                .customFields(source.customFields)
                .collectionSizes(source.collectionSizes)
                .ignoredFields(source.ignoredFields);
    }

    public static final class Builder {
        private Faker faker;
        private final Map<String, Object> customFields = new HashMap<String, Object>();
        private final Map<String, Integer> collectionSizes = new HashMap<String, Integer>();
        private final Set<String> ignoredFields = new HashSet<String>();
        private Set<Class<?>> generatingTypes = new HashSet<Class<?>>();
        private int defaultCollectionSize = DEFAULT_COLLECTION_SIZE;

        public Builder faker(Faker faker) {
            this.faker = faker;
            return this;
        }

        public Builder field(String name, Object value) {
            customFields.put(name, value);
            return this;
        }

        public Builder size(String name, int count) {
            collectionSizes.put(name, count);
            return this;
        }

        public Builder ignore(String name) {
            ignoredFields.add(name);
            return this;
        }

        public Builder defaultCollectionSize(int size) {
            this.defaultCollectionSize = size;
            return this;
        }

        Builder generatingTypes(Set<Class<?>> types) {
            this.generatingTypes = types;
            return this;
        }

        Builder customFields(Map<String, Object> fields) {
            this.customFields.putAll(fields);
            return this;
        }

        Builder collectionSizes(Map<String, Integer> sizes) {
            this.collectionSizes.putAll(sizes);
            return this;
        }

        Builder ignoredFields(Set<String> ignored) {
            this.ignoredFields.addAll(ignored);
            return this;
        }

        public GenerationContext build() {
            return new GenerationContext(this);
        }
    }
}
