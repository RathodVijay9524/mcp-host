package com.vijay.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for exposing application metrics
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    
    private final MeterRegistry meterRegistry;
    
    public MetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * Get application metrics summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get counter values
        Counter chatRequests = meterRegistry.find("chat.requests.total").counter();
        Counter chatSuccess = meterRegistry.find("chat.responses.success").counter();
        Counter chatErrors = meterRegistry.find("chat.responses.error").counter();
        Counter mcpToolCalls = meterRegistry.find("mcp.tool.calls.total").counter();
        Counter cacheHits = meterRegistry.find("cache.hits").counter();
        Counter cacheMisses = meterRegistry.find("cache.misses").counter();
        
        // Get timer statistics
        Timer chatResponseTimer = meterRegistry.find("chat.response.time").timer();
        
        metrics.put("chatRequests", chatRequests != null ? chatRequests.count() : 0);
        metrics.put("chatSuccess", chatSuccess != null ? chatSuccess.count() : 0);
        metrics.put("chatErrors", chatErrors != null ? chatErrors.count() : 0);
        metrics.put("mcpToolCalls", mcpToolCalls != null ? mcpToolCalls.count() : 0);
        metrics.put("cacheHits", cacheHits != null ? cacheHits.count() : 0);
        metrics.put("cacheMisses", cacheMisses != null ? cacheMisses.count() : 0);
        
        if (chatResponseTimer != null) {
            Map<String, Object> responseTimeStats = new HashMap<>();
            responseTimeStats.put("count", chatResponseTimer.count());
            responseTimeStats.put("mean", chatResponseTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            responseTimeStats.put("max", chatResponseTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS));
            responseTimeStats.put("p95", chatResponseTimer.percentile(0.95, java.util.concurrent.TimeUnit.MILLISECONDS));
            responseTimeStats.put("p99", chatResponseTimer.percentile(0.99, java.util.concurrent.TimeUnit.MILLISECONDS));
            metrics.put("responseTimeStats", responseTimeStats);
        }
        
        // Calculate success rate
        double totalRequests = (chatRequests != null ? chatRequests.count() : 0);
        double successCount = (chatSuccess != null ? chatSuccess.count() : 0);
        double successRate = totalRequests > 0 ? (successCount / totalRequests) * 100 : 0;
        metrics.put("successRate", Math.round(successRate * 100.0) / 100.0);
        
        // Calculate cache hit rate
        double totalCacheRequests = (cacheHits != null ? cacheHits.count() : 0) + (cacheMisses != null ? cacheMisses.count() : 0);
        double hitCount = (cacheHits != null ? cacheHits.count() : 0);
        double cacheHitRate = totalCacheRequests > 0 ? (hitCount / totalCacheRequests) * 100 : 0;
        metrics.put("cacheHitRate", Math.round(cacheHitRate * 100.0) / 100.0);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get detailed metrics by provider
     */
    @GetMapping("/by-provider")
    public ResponseEntity<Map<String, Object>> getMetricsByProvider() {
        Map<String, Object> providerMetrics = new HashMap<>();
        
        // Get all provider-specific counters
        meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().contains("by_provider"))
                .forEach(meter -> {
                    String provider = meter.getId().getTag("provider");
                    String model = meter.getId().getTag("model");
                    String key = provider + "_" + model;
                    
                    if (!providerMetrics.containsKey(key)) {
                        providerMetrics.put(key, new HashMap<String, Object>());
                    }
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> providerData = (Map<String, Object>) providerMetrics.get(key);
                    providerData.put(meter.getId().getName(), meter.measure().iterator().next().getValue());
                });
        
        return ResponseEntity.ok(providerMetrics);
    }
    
    /**
     * Get JVM metrics
     */
    @GetMapping("/jvm")
    public ResponseEntity<Map<String, Object>> getJvmMetrics() {
        Map<String, Object> jvmMetrics = new HashMap<>();
        
        // Memory metrics
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        jvmMetrics.put("maxMemory", maxMemory);
        jvmMetrics.put("totalMemory", totalMemory);
        jvmMetrics.put("freeMemory", freeMemory);
        jvmMetrics.put("usedMemory", usedMemory);
        jvmMetrics.put("memoryUsagePercent", Math.round((double) usedMemory / maxMemory * 100 * 100.0) / 100.0);
        
        // Thread metrics
        jvmMetrics.put("activeThreads", Thread.activeCount());
        jvmMetrics.put("availableProcessors", runtime.availableProcessors());
        
        return ResponseEntity.ok(jvmMetrics);
    }
}
