package com.budgettracker.exception;

import java.util.Map;

/**
 * Exception for validation errors
 */
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = null;
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
