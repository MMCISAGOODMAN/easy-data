package com.easydata.generator;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.easydata.GenerationContext;

/**
 * Generates List, Set, and array instances with configurable element count.
 */
final class CollectionGenerator implements FieldGenerator {

    private final FieldGeneratorRegistry registry;

    CollectionGenerator(FieldGeneratorRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.isArray()
                || Collection.class.isAssignableFrom(type)
                || List.class.isAssignableFrom(type)
                || Set.class.isAssignableFrom(type);
    }

    @Override
    public Object generate(Class<?> type, Type generic, GenerationContext context) {
        Class<?> elementType = resolveElementType(type, generic);
        int size = context.getDefaultCollectionSize();
        if (type.isArray()) {
            return generateArray(elementType, size, context);
        }
        if (Set.class.isAssignableFrom(type)) {
            Set<Object> set = createSet(type, size);
            fillCollection(set, elementType, generic, size, context);
            return set;
        }
        if (List.class.isAssignableFrom(type)) {
            List<Object> list = new ArrayList<Object>(size);
            fillCollection(list, elementType, generic, size, context);
            return list;
        }
        if (Collection.class.isAssignableFrom(type)) {
            List<Object> list = new ArrayList<Object>(size);
            fillCollection(list, elementType, generic, size, context);
            return list;
        }
        List<Object> list = new ArrayList<Object>(size);
        fillCollection(list, elementType, generic, size, context);
        return list;
    }

    private static Set<Object> createSet(Class<?> type, int size) {
        if (SortedSet.class.isAssignableFrom(type) || TreeSet.class.isAssignableFrom(type)) {
            return new TreeSet<Object>();
        }
        if (LinkedHashSet.class.isAssignableFrom(type)) {
            return new LinkedHashSet<Object>(size);
        }
        return new HashSet<Object>(size);
    }

    private void fillCollection(Collection<Object> collection, Class<?> elementType,
                                Type generic, int size, GenerationContext context) {
        Type elementGeneric = resolveElementGeneric(generic);
        for (int i = 0; i < size; i++) {
            collection.add(registry.generateValue(elementType, elementGeneric, context));
        }
    }

    private Object generateArray(Class<?> elementType, int size, GenerationContext context) {
        Object array = Array.newInstance(elementType, size);
        for (int i = 0; i < size; i++) {
            Object value = registry.generateValue(elementType, null, context);
            Array.set(array, i, value);
        }
        return array;
    }

    static Class<?> resolveElementType(Class<?> rawType, Type generic) {
        if (rawType.isArray()) {
            return rawType.getComponentType();
        }
        if (generic instanceof ParameterizedType) {
            Type arg = ((ParameterizedType) generic).getActualTypeArguments()[0];
            return toClass(arg);
        }
        return Object.class;
    }

    static Type resolveElementGeneric(Type generic) {
        if (generic instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) generic).getActualTypeArguments();
            if (args.length > 0) {
                return args[0];
            }
        }
        return null;
    }

    static Class<?> toClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return toClass(((ParameterizedType) type).getRawType());
        }
        return Object.class;
    }
}
