package com.easydata.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class for EasyData generation hints.
 * <p>
 * Validated at compile time by the {@code easy-data-processor} module:
 * concrete class, no-arg constructor, populate-friendly field types.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EasyData {

    /** Default collection size when not overridden by builder. */
    int defaultSize() default 3;

    /**
     * Marks a field to skip during generation.
     * Validated at compile time: must not be static or final.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Ignore {
    }

    /**
     * Sets a fixed literal value for a scalar field at generation time.
     *
     * <pre>{@code
     * @EasyData.Field("张三")
     * private String name;
     * }</pre>
     *
     * Supported types: String, primitives, wrappers, enum, BigDecimal, BigInteger.
     * Validated at compile time for type compatibility.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Field {
        String value();
    }
}
