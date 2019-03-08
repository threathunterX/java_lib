package com.threathunter.variable.exception;

/**
 * This means the required operation is not supported.
 *
 * @author Wen Lu
 */
public class NotSupportException extends RuntimeException {
    public NotSupportException() {
        super();
    }

    public NotSupportException(String message) {
        super(message);
    }

    public NotSupportException(String message, Throwable cause) {
        super(message, cause);
    }
}
