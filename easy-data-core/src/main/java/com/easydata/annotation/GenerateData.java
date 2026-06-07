package com.easydata.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import com.easydata.jupiter.EasyDataExtension;

/**
 * Injects auto-generated test data into JUnit 5 test method parameters.
 *
 * <pre>{@code
 * @Test
 * @GenerateData
 * void testCreateUser(User user) {
 *     assertNotNull(user.getName());
 * }
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@ExtendWith(EasyDataExtension.class)
public @interface GenerateData {
}
