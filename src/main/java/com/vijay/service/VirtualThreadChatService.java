package com.vijay.service;

import com.vijay.model.ChatRequest;
import com.vijay.model.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Service demonstrating Virtual Threads usage for high-concurrency chat processing
 * Virtual Threads are perfect for I/O-bound operations like AI API calls
 */
@Service
public class VirtualThreadChatService {

    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadChatService.class);

    private final ChatService chatService;
    private final Executor virtualThreadExecutor;

    public VirtualThreadChatService(ChatService chatService, 
                                   @Qualifier("virtualThreadExecutor") Executor virtualThreadExecutor) {
        this.chatService = chatService;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    /**
     * Process chat request using Virtual Threads
     * This can handle thousands of concurrent requests without blocking platform threads
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<ChatResponse> processChatAsync(ChatRequest request, String conversationId) {
        logger.info("Processing chat request with Virtual Thread for conversation: {}", conversationId);
        
        try {
            // Simulate some I/O-bound work (AI API call)
            ChatResponse response = chatService.processChatRequest(request, conversationId);
            
            logger.info("Successfully processed chat request with Virtual Thread for conversation: {}", conversationId);
            return CompletableFuture.completedFuture(response);
            
        } catch (Exception e) {
            logger.error("Error processing chat request with Virtual Thread for conversation {}: {}", 
                        conversationId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Process multiple chat requests concurrently using Virtual Threads
     * Demonstrates the power of Virtual Threads for concurrent processing
     */
    public CompletableFuture<ChatResponse[]> processMultipleChatsAsync(ChatRequest[] requests, String[] conversationIds) {
        logger.info("Processing {} chat requests concurrently with Virtual Threads", requests.length);
        
        CompletableFuture<ChatResponse>[] futures = new CompletableFuture[requests.length];
        
        for (int i = 0; i < requests.length; i++) {
            futures[i] = processChatAsync(requests[i], conversationIds[i]);
        }
        
        return CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    ChatResponse[] responses = new ChatResponse[requests.length];
                    for (int i = 0; i < futures.length; i++) {
                        try {
                            responses[i] = futures[i].get();
                        } catch (Exception e) {
                            logger.error("Error getting result from future {}: {}", i, e.getMessage());
                            // Create error response
                            responses[i] = new ChatResponse(
                                requests[i].getProvider() != null ? requests[i].getProvider() : "unknown",
                                requests[i].getModel() != null ? requests[i].getModel() : "unknown",
                                "Error processing request: " + e.getMessage()
                            );
                        }
                    }
                    return responses;
                });
    }

    /**
     * Get Virtual Thread information for monitoring
     */
    public String getVirtualThreadInfo() {
        Thread currentThread = Thread.currentThread();
        return String.format("""
            Virtual Thread Info:
            - Thread Name: %s
            - Is Virtual: %s
            - Thread ID: %d
            - Thread Group: %s
            - Priority: %d
            - State: %s
            """, 
            currentThread.getName(),
            currentThread.isVirtual(),
            currentThread.getId(),
            currentThread.getThreadGroup().getName(),
            currentThread.getPriority(),
            currentThread.getState()
        );
    }
}
