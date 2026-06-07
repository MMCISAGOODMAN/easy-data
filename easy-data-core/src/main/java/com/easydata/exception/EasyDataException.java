package com.easydata.exception;

/**
 * Root exception for all EasyData failures.
 */
public class EasyDataException extends RuntimeException {

    public EasyDataException(String message) {
        super(message);
    }

    public EasyDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
