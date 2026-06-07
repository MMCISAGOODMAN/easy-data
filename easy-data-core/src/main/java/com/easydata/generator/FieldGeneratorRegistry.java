package com.easydata.generator;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.easydata.DataGenerator;
import com.easydata.GenerationContext;

/**
 * Registry that picks the first matching {@link FieldGenerator} for a type.
 */
public final class FieldGeneratorRegistry {

    private DataGenerator dataGenerator;
    private List<FieldGenerator> generators;

    private FieldGeneratorRegistry() {
    }

    public static FieldGeneratorRegistry createFor(DataGenerator dataGenerator) {
        FieldGeneratorRegistry registry = new FieldGeneratorRegistry();
        registry.dataGenerator = dataGenerator;
        registry.generators = Arrays.asList(
                new StringGenerator(),
                new NumberGenerator(),
                new BooleanGenerator(),
                new DateTimeGenerator(),
                new EnumGenerator(),
                new CollectionGenerator(registry),
                new MapGenerator(registry),
                new ObjectFieldGenerator(dataGenerator)
        );
        return registry;
    }

    public FieldGenerator resolve(Class<?> type) {
        for (FieldGenerator generator : generators) {
            if (generator.supports(type)) {
                return generator;
            }
        }
        throw new IllegalStateException("No generator found for type: " + type.getName());
    }

    public Object generateValue(Class<?> type, Type generic, GenerationContext context) {
        return resolve(type).generate(type, generic, context);
    }
}
