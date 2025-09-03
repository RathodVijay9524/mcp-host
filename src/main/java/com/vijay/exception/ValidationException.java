package com.vijay.exception;

/**
 * Exception for validation related errors
 */
public class ValidationException extends ChatServiceException {
    
    private final String field;
    
    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", message, 400);
        this.field = field;
    }
    
    public ValidationException(String field, String message, Throwable cause) {
        super("VALIDATION_ERROR", message, 400, cause);
        this.field = field;
    }
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, 400);
        this.field = null;
    }
    
    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, 400, cause);
        this.field = null;
    }
    
    public String getField() {
        return field;
    }
}
