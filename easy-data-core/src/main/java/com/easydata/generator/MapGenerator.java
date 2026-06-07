package com.easydata.generator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.easydata.GenerationContext;
import com.easydata.exception.GenerationException;

/**
 * Generates Map instances with configurable entry count (default 3).
 */
final class MapGenerator implements FieldGenerator {

    private final FieldGeneratorRegistry registry;

    MapGenerator(FieldGeneratorRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean supports(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Type generic, GenerationContext context) {
        Class<?> keyType = String.class;
        Class<?> valueType = Object.class;
        Type valueGeneric = null;

        if (generic instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) generic).getActualTypeArguments();
            if (args.length >= 1) {
                keyType = CollectionGenerator.toClass(args[0]);
            }
            if (args.length >= 2) {
                valueType = CollectionGenerator.toClass(args[1]);
                valueGeneric = args[1];
            }
        }

        Map<Object, Object> map = createMap(type);
        int size = context.getDefaultCollectionSize();
        for (int i = 0; i < size; i++) {
            Object key = registry.generateValue(keyType, null, context);
            Object value = registry.generateValue(valueType, valueGeneric, context);
            map.put(key, value);
        }
        return map;
    }

    private static Map<Object, Object> createMap(Class<?> type) {
        if (SortedMap.class.isAssignableFrom(type) || TreeMap.class.isAssignableFrom(type)) {
            return new TreeMap<Object, Object>();
        }
        if (LinkedHashMap.class.isAssignableFrom(type)) {
            return new LinkedHashMap<Object, Object>();
        }
        if (HashMap.class.isAssignableFrom(type) || type == Map.class) {
            return new HashMap<Object, Object>();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) type.newInstance();
            return map;
        } catch (Exception e) {
            throw new GenerationException("Cannot instantiate map type: " + type.getName(), e);
        }
    }
}
