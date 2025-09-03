package com.vijay.controller;

import com.vijay.model.ChatRequest;
import com.vijay.model.ChatResponse;
import com.vijay.service.VirtualThreadChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Controller demonstrating Virtual Threads usage for high-concurrency chat processing
 */
@RestController
@RequestMapping("/api/virtual-threads")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class VirtualThreadController {

    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadController.class);

    private final VirtualThreadChatService virtualThreadChatService;

    public VirtualThreadController(VirtualThreadChatService virtualThreadChatService) {
        this.virtualThreadChatService = virtualThreadChatService;
    }

    /**
     * Process a single chat request using Virtual Threads
     */
    @PostMapping("/chat")
    public CompletableFuture<ResponseEntity<ChatResponse>> processChatAsync(@RequestBody ChatRequest request) {
        logger.info("Received async chat request with Virtual Threads");
        
        String conversationId = UUID.randomUUID().toString();
        
        return virtualThreadChatService.processChatAsync(request, conversationId)
                .thenApply(response -> {
                    logger.info("Successfully processed async chat request for conversation: {}", conversationId);
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    logger.error("Error processing async chat request: {}", throwable.getMessage(), throwable);
                    return ResponseEntity.internalServerError().build();
                });
    }

    /**
     * Process multiple chat requests concurrently using Virtual Threads
     * Demonstrates the power of Virtual Threads for concurrent processing
     */
    @PostMapping("/chat/batch")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> processMultipleChatsAsync(
            @RequestBody ChatRequest[] requests) {
        
        logger.info("Processing {} chat requests concurrently with Virtual Threads", requests.length);
        
        // Generate conversation IDs
        String[] conversationIds = new String[requests.length];
        for (int i = 0; i < requests.length; i++) {
            conversationIds[i] = UUID.randomUUID().toString();
        }
        
        return virtualThreadChatService.processMultipleChatsAsync(requests, conversationIds)
                .thenApply(responses -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("totalRequests", requests.length);
                    result.put("responses", responses);
                    result.put("conversationIds", conversationIds);
                    result.put("message", "All requests processed concurrently with Virtual Threads");
                    
                    logger.info("Successfully processed {} chat requests concurrently", requests.length);
                    return ResponseEntity.ok(result);
                })
                .exceptionally(throwable -> {
                    logger.error("Error processing multiple chat requests: {}", throwable.getMessage(), throwable);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("error", "Failed to process requests");
                    errorResult.put("message", throwable.getMessage());
                    return ResponseEntity.internalServerError().body(errorResult);
                });
    }

    /**
     * Get Virtual Thread information for monitoring
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getVirtualThreadInfo() {
        logger.info("Getting Virtual Thread information");
        
        Map<String, Object> info = new HashMap<>();
        info.put("virtualThreadInfo", virtualThreadChatService.getVirtualThreadInfo());
        info.put("currentThread", Thread.currentThread().getName());
        info.put("isVirtual", Thread.currentThread().isVirtual());
        info.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(info);
    }

    /**
     * Test endpoint to demonstrate Virtual Threads performance
     */
    @PostMapping("/test/concurrency")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> testConcurrency(
            @RequestParam(defaultValue = "10") int requestCount) {
        
        logger.info("Testing Virtual Threads concurrency with {} requests", requestCount);
        
        // Create test requests
        ChatRequest[] testRequests = new ChatRequest[requestCount];
        String[] conversationIds = new String[requestCount];
        
        for (int i = 0; i < requestCount; i++) {
            testRequests[i] = new ChatRequest();
            testRequests[i].setMessage("Test message " + (i + 1));
            testRequests[i].setProvider("gemini");
            testRequests[i].setModel("gemini-1.5-flash");
            
            conversationIds[i] = "test-" + UUID.randomUUID().toString();
        }
        
        long startTime = System.currentTimeMillis();
        
        return virtualThreadChatService.processMultipleChatsAsync(testRequests, conversationIds)
                .thenApply(responses -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("requestCount", requestCount);
                    result.put("duration", duration + " ms");
                    result.put("averageTimePerRequest", (duration / (double) requestCount) + " ms");
                    result.put("successfulRequests", Arrays.stream(responses)
                            .mapToInt(r -> r != null ? 1 : 0)
                            .sum());
                    result.put("message", "Virtual Threads concurrency test completed");
                    
                    logger.info("Virtual Threads concurrency test completed: {} requests in {} ms", 
                               requestCount, duration);
                    
                    return ResponseEntity.ok(result);
                })
                .exceptionally(throwable -> {
                    logger.error("Error in Virtual Threads concurrency test: {}", throwable.getMessage(), throwable);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("error", "Concurrency test failed");
                    errorResult.put("message", throwable.getMessage());
                    return ResponseEntity.internalServerError().body(errorResult);
                });
    }
}
