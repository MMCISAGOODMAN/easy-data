package com.easydata;

/**
 * Zero-config test data generator — one line to fill complex objects.
 *
 * <pre>{@code
 * User user = EasyData.generate(User.class);
 *
 * User custom = EasyData.of(User.class)
 *     .field("name", "张三")
 *     .size("orders", 5)
 *     .ignore("password")
 *     .generate();
 * }</pre>
 */
public final class EasyData {

    private static final DataGenerator GENERATOR = new DataGenerator();

    private EasyData() {
    }

    /**
     * Generates a fully populated instance with sensible defaults.
     */
    public static <T> T generate(Class<T> type) {
        return GENERATOR.generate(type);
    }

    /**
     * Returns a builder for customizing field values, sizes, and ignored fields.
     */
    public static <T> EasyDataBuilder<T> of(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        return new EasyDataBuilder<T>(type, GENERATOR);
    }
}
