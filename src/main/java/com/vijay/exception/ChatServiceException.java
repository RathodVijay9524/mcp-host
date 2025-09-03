package com.vijay.exception;

/**
 * Base exception for chat service related errors
 */
public class ChatServiceException extends RuntimeException {
    
    private final String errorCode;
    private final int httpStatus;
    
    public ChatServiceException(String message) {
        super(message);
        this.errorCode = "CHAT_SERVICE_ERROR";
        this.httpStatus = 500;
    }
    
    public ChatServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CHAT_SERVICE_ERROR";
        this.httpStatus = 500;
    }
    
    public ChatServiceException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public ChatServiceException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
}
