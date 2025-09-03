package com.vijay.exception;

import com.vijay.model.ChatResponse;
import com.vijay.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle ChatServiceException
     */
    @ExceptionHandler(ChatServiceException.class)
    public ResponseEntity<ErrorResponse> handleChatServiceException(ChatServiceException ex, WebRequest request) {
        logger.error("ChatServiceException: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getHttpStatus())
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }
    
    /**
     * Handle AIProviderException
     */
    @ExceptionHandler(AIProviderException.class)
    public ResponseEntity<ErrorResponse> handleAIProviderException(AIProviderException ex, WebRequest request) {
        logger.error("AIProviderException for provider {}: {} - {}", ex.getProvider(), ex.getErrorCode(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getHttpStatus())
                .error(ex.getErrorCode())
                .message(String.format("AI Provider '%s' error: %s", ex.getProvider(), ex.getMessage()))
                .path(request.getDescription(false).replace("uri=", ""))
                .details(Map.of("provider", ex.getProvider()))
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }
    
    /**
     * Handle MCPConnectionException
     */
    @ExceptionHandler(MCPConnectionException.class)
    public ResponseEntity<ErrorResponse> handleMCPConnectionException(MCPConnectionException ex, WebRequest request) {
        logger.error("MCPConnectionException for server {}: {} - {}", ex.getServerName(), ex.getErrorCode(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getHttpStatus())
                .error(ex.getErrorCode())
                .message(String.format("MCP Server '%s' connection error: %s", ex.getServerName(), ex.getMessage()))
                .path(request.getDescription(false).replace("uri=", ""))
                .details(Map.of("serverName", ex.getServerName()))
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }
    
    /**
     * Handle ValidationException
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, WebRequest request) {
        logger.warn("ValidationException: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getHttpStatus())
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .details(ex.getField() != null ? Map.of("field", ex.getField()) : Map.of())
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }
    
    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("Validation error: {}", ex.getMessage());
        
        Map<String, Object> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Request validation failed")
                .path(request.getDescription(false).replace("uri=", ""))
                .details(fieldErrors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle generic RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("RuntimeException: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Handle generic Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unexpected exception: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
