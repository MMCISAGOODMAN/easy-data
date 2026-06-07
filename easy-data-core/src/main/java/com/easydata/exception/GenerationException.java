package com.easydata.exception;

/**
 * Thrown when test data generation fails for a specific type or field.
 */
public class GenerationException extends EasyDataException {

    public GenerationException(String message) {
        super(message);
    }

    public GenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
