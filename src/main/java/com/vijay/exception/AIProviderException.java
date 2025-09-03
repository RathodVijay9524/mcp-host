package com.vijay.exception;

/**
 * Exception for AI provider related errors
 */
public class AIProviderException extends ChatServiceException {
    
    private final String provider;
    
    public AIProviderException(String provider, String message) {
        super("AI_PROVIDER_ERROR", message, 502);
        this.provider = provider;
    }
    
    public AIProviderException(String provider, String message, Throwable cause) {
        super("AI_PROVIDER_ERROR", message, 502, cause);
        this.provider = provider;
    }
    
    public AIProviderException(String provider, String errorCode, String message, int httpStatus) {
        super(errorCode, message, httpStatus);
        this.provider = provider;
    }
    
    public AIProviderException(String provider, String errorCode, String message, int httpStatus, Throwable cause) {
        super(errorCode, message, httpStatus, cause);
        this.provider = provider;
    }
    
    public String getProvider() {
        return provider;
    }
}
