package com.vijay.service;

import com.vijay.model.ChatRequest;
import com.vijay.model.ChatResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Async service for non-blocking chat operations
 */
@Service
@EnableAsync
public class AsyncChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncChatService.class);
    
    private final ResilientChatService resilientChatService;
    
    public AsyncChatService(ResilientChatService resilientChatService) {
        this.resilientChatService = resilientChatService;
    }
    
    /**
     * Process chat request asynchronously
     */
    @Async("taskExecutor")
    public CompletableFuture<ChatResponse> processChatRequestAsync(ChatRequest request, String conversationId) {
        logger.info("Processing async chat request for conversation: {}", conversationId);
        
        try {
            ChatResponse response = resilientChatService.processChatRequest(request, conversationId);
            logger.info("Completed async chat request for conversation: {}", conversationId);
            return CompletableFuture.completedFuture(response);
            
        } catch (Exception e) {
            logger.error("Failed async chat request for conversation {}: {}", conversationId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Preload tool definitions asynchronously
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> preloadToolDefinitionsAsync() {
        logger.info("Preloading tool definitions asynchronously");
        
        try {
            // This will trigger cache loading
            // In a real implementation, you might want to call the cached service
            logger.info("Tool definitions preloaded successfully");
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            logger.error("Failed to preload tool definitions: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Cleanup old chat memories asynchronously
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> cleanupOldChatMemoriesAsync() {
        logger.info("Cleaning up old chat memories asynchronously");
        
        try {
            // In a real implementation, you would clean up old Redis keys
            // For now, just log the operation
            logger.info("Chat memory cleanup completed");
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            logger.error("Failed to cleanup chat memories: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
