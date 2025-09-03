package com.vijay.service;

import com.vijay.exception.AIProviderException;
import com.vijay.exception.ChatServiceException;
import com.vijay.model.ChatRequest;
import com.vijay.model.ChatResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Resilient wrapper for ChatService with circuit breaker, retry, and timeout patterns
 */
@Service
public class ResilientChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ResilientChatService.class);
    
    private final ChatService chatService;
    private final CircuitBreaker aiProviderCircuitBreaker;
    private final CircuitBreaker mcpServerCircuitBreaker;
    private final Retry aiProviderRetry;
    private final Retry mcpServerRetry;
    private final TimeLimiter aiProviderTimeLimiter;
    private final TimeLimiter mcpServerTimeLimiter;
    
    public ResilientChatService(ChatService chatService,
                               CircuitBreakerRegistry circuitBreakerRegistry,
                               RetryRegistry retryRegistry,
                               TimeLimiterRegistry timeLimiterRegistry) {
        this.chatService = chatService;
        this.aiProviderCircuitBreaker = circuitBreakerRegistry.circuitBreaker("ai-provider");
        this.mcpServerCircuitBreaker = circuitBreakerRegistry.circuitBreaker("mcp-server");
        this.aiProviderRetry = retryRegistry.retry("ai-provider");
        this.mcpServerRetry = retryRegistry.retry("mcp-server");
        this.aiProviderTimeLimiter = timeLimiterRegistry.timeLimiter("ai-provider");
        this.mcpServerTimeLimiter = timeLimiterRegistry.timeLimiter("mcp-server");
        
        // Add event listeners for monitoring
        setupCircuitBreakerEventListeners();
        setupRetryEventListeners();
    }
    
    /**
     * Process chat request with resilience patterns
     */
    public ChatResponse processChatRequest(ChatRequest request, String conversationId) {
        logger.info("Processing resilient chat request for conversation: {}", conversationId);
        
        // Determine which circuit breaker to use based on provider
        CircuitBreaker circuitBreaker = getCircuitBreakerForProvider(request.getProvider());
        Retry retry = getRetryForProvider(request.getProvider());
        TimeLimiter timeLimiter = getTimeLimiterForProvider(request.getProvider());
        
        // Create the supplier with resilience patterns
        Supplier<ChatResponse> supplier = () -> {
            try {
                return chatService.processChatRequest(request, conversationId);
            } catch (Exception e) {
                logger.error("Error in chat service call: {}", e.getMessage(), e);
                throw new ChatServiceException("Failed to process chat request", e);
            }
        };
        
        // Apply resilience patterns
        Supplier<ChatResponse> resilientSupplier = Retry.decorateSupplier(retry, supplier);
        resilientSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, resilientSupplier);
        
        try {
            // Execute with timeout
            CompletableFuture<ChatResponse> future = CompletableFuture.supplyAsync(resilientSupplier);
            return timeLimiter.executeFutureSupplier(() -> future);
            
        } catch (Exception e) {
            logger.error("Resilient chat request failed for conversation {}: {}", conversationId, e.getMessage(), e);
            
            // Determine the type of exception and throw appropriate custom exception
            if (e.getCause() instanceof AIProviderException) {
                throw (AIProviderException) e.getCause();
            } else if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
                throw new AIProviderException(
                    request.getProvider() != null ? request.getProvider() : "unknown",
                    "AI Provider circuit breaker is OPEN - service temporarily unavailable",
                    "CIRCUIT_BREAKER_OPEN",
                    503,
                    e
                );
            } else {
                throw new ChatServiceException("Failed to process chat request with resilience patterns", e);
            }
        }
    }
    
    private CircuitBreaker getCircuitBreakerForProvider(String provider) {
        if ("ollama".equals(provider)) {
            return mcpServerCircuitBreaker; // Ollama is local, use MCP circuit breaker
        } else {
            return aiProviderCircuitBreaker; // External AI providers
        }
    }
    
    private Retry getRetryForProvider(String provider) {
        if ("ollama".equals(provider)) {
            return mcpServerRetry;
        } else {
            return aiProviderRetry;
        }
    }
    
    private TimeLimiter getTimeLimiterForProvider(String provider) {
        if ("ollama".equals(provider)) {
            return mcpServerTimeLimiter;
        } else {
            return aiProviderTimeLimiter;
        }
    }
    
    private void setupCircuitBreakerEventListeners() {
        aiProviderCircuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    logger.info("AI Provider Circuit Breaker state transition: {} -> {}", 
                        event.getStateTransition().getFromState(), 
                        event.getStateTransition().getToState()));
        
        mcpServerCircuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    logger.info("MCP Server Circuit Breaker state transition: {} -> {}", 
                        event.getStateTransition().getFromState(), 
                        event.getStateTransition().getToState()));
    }
    
    private void setupRetryEventListeners() {
        aiProviderRetry.getEventPublisher()
                .onRetry(event -> 
                    logger.warn("AI Provider retry attempt {} failed: {}", 
                        event.getNumberOfRetryAttempts(), event.getLastThrowable().getMessage()));
        
        mcpServerRetry.getEventPublisher()
                .onRetry(event -> 
                    logger.warn("MCP Server retry attempt {} failed: {}", 
                        event.getNumberOfRetryAttempts(), event.getLastThrowable().getMessage()));
    }
    
    /**
     * Get available providers
     */
    public String[] getAvailableProviders() {
        return chatService.getAvailableProviders();
    }
    
    /**
     * Get available models for a provider
     */
    public String[] getAvailableModels(String provider) {
        return chatService.getAvailableModels(provider);
    }
}
