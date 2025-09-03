package com.vijay;

import com.vijay.model.ChatRequest;
import com.vijay.service.VirtualThreadChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance test for Virtual Threads implementation
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.threads.virtual.enabled=true"
})
public class VirtualThreadsPerformanceTest {

    @Autowired
    private VirtualThreadChatService virtualThreadChatService;

    @Test
    public void testVirtualThreadsPerformance() throws Exception {
        System.out.println("=== Virtual Threads Performance Test ===");
        
        // Test with different concurrency levels
        int[] concurrencyLevels = {10, 50, 100, 200};
        
        for (int concurrency : concurrencyLevels) {
            System.out.println("\n--- Testing with " + concurrency + " concurrent requests ---");
            
            long startTime = System.currentTimeMillis();
            
            // Create test requests
            ChatRequest[] requests = new ChatRequest[concurrency];
            String[] conversationIds = new String[concurrency];
            
            for (int i = 0; i < concurrency; i++) {
                requests[i] = new ChatRequest();
                requests[i].setMessage("Test message " + (i + 1));
                requests[i].setProvider("gemini");
                requests[i].setModel("gemini-1.5-flash");
                
                conversationIds[i] = "test-" + i;
            }
            
            // Process requests concurrently
            CompletableFuture<com.vijay.model.ChatResponse[]> future = 
                virtualThreadChatService.processMultipleChatsAsync(requests, conversationIds);
            
            // Wait for completion
            com.vijay.model.ChatResponse[] responses = future.get(30, TimeUnit.SECONDS);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            System.out.println("Concurrency Level: " + concurrency);
            System.out.println("Total Duration: " + duration + " ms");
            System.out.println("Average Time per Request: " + (duration / (double) concurrency) + " ms");
            System.out.println("Successful Requests: " + responses.length);
            
            // Verify all requests were processed
            assertEquals(concurrency, responses.length);
        }
    }

    @Test
    public void testVirtualThreadsVsPlatformThreads() throws Exception {
        System.out.println("\n=== Virtual Threads vs Platform Threads Comparison ===");
        
        int requestCount = 100;
        
        // Test with Virtual Threads
        System.out.println("\n--- Testing with Virtual Threads ---");
        long virtualStartTime = System.currentTimeMillis();
        
        ChatRequest[] requests = new ChatRequest[requestCount];
        String[] conversationIds = new String[requestCount];
        
        for (int i = 0; i < requestCount; i++) {
            requests[i] = new ChatRequest();
            requests[i].setMessage("Virtual Thread test " + (i + 1));
            requests[i].setProvider("gemini");
            requests[i].setModel("gemini-1.5-flash");
            
            conversationIds[i] = "virtual-test-" + i;
        }
        
        CompletableFuture<com.vijay.model.ChatResponse[]> virtualFuture = 
            virtualThreadChatService.processMultipleChatsAsync(requests, conversationIds);
        
        com.vijay.model.ChatResponse[] virtualResponses = virtualFuture.get(30, TimeUnit.SECONDS);
        long virtualEndTime = System.currentTimeMillis();
        long virtualDuration = virtualEndTime - virtualStartTime;
        
        System.out.println("Virtual Threads Duration: " + virtualDuration + " ms");
        System.out.println("Virtual Threads Average: " + (virtualDuration / (double) requestCount) + " ms per request");
        
        // Test with Platform Threads (traditional thread pool)
        System.out.println("\n--- Testing with Platform Threads ---");
        long platformStartTime = System.currentTimeMillis();
        
        Executor platformExecutor = Executors.newFixedThreadPool(10); // Limited to 10 threads
        
        CompletableFuture<com.vijay.model.ChatResponse>[] platformFutures = new CompletableFuture[requestCount];
        
        for (int i = 0; i < requestCount; i++) {
            final int index = i;
            platformFutures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    ChatRequest request = new ChatRequest();
                    request.setMessage("Platform Thread test " + (index + 1));
                    request.setProvider("gemini");
                    request.setModel("gemini-1.5-flash");
                    
                    return virtualThreadChatService.processChatAsync(request, "platform-test-" + index).get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, platformExecutor);
        }
        
        CompletableFuture<Void> allPlatformFutures = CompletableFuture.allOf(platformFutures);
        allPlatformFutures.get(30, TimeUnit.SECONDS);
        
        long platformEndTime = System.currentTimeMillis();
        long platformDuration = platformEndTime - platformStartTime;
        
        System.out.println("Platform Threads Duration: " + platformDuration + " ms");
        System.out.println("Platform Threads Average: " + (platformDuration / (double) requestCount) + " ms per request");
        
        // Compare results
        System.out.println("\n--- Comparison Results ---");
        System.out.println("Virtual Threads: " + virtualDuration + " ms");
        System.out.println("Platform Threads: " + platformDuration + " ms");
        System.out.println("Performance Improvement: " + 
            ((double) platformDuration / virtualDuration) + "x faster with Virtual Threads");
        
        // Virtual Threads should be faster for I/O-bound operations
        assertTrue(virtualDuration <= platformDuration, 
            "Virtual Threads should be faster or equal to Platform Threads for I/O-bound operations");
    }

    @Test
    public void testVirtualThreadInfo() {
        System.out.println("\n=== Virtual Thread Information ===");
        
        String threadInfo = virtualThreadChatService.getVirtualThreadInfo();
        System.out.println(threadInfo);
        
        // Verify thread info contains expected information
        assertTrue(threadInfo.contains("Thread Name"));
        assertTrue(threadInfo.contains("Is Virtual"));
        assertTrue(threadInfo.contains("Thread ID"));
    }
}
