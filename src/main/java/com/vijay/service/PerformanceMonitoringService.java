package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for monitoring application performance
 */
@Service
public class PerformanceMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringService.class);
    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");
    
    private final ConcurrentHashMap<String, AtomicLong> requestCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> totalResponseTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> maxResponseTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> minResponseTime = new ConcurrentHashMap<>();
    
    /**
     * Start timing a request
     */
    public Instant startTiming() {
        return Instant.now();
    }
    
    /**
     * End timing and record metrics
     */
    public void endTiming(Instant startTime, String operation, String provider, String model) {
        Duration duration = Duration.between(startTime, Instant.now());
        long responseTimeMs = duration.toMillis();
        
        String key = provider + "_" + model + "_" + operation;
        
        // Update counters
        requestCounters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        totalResponseTime.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(responseTimeMs);
        
        // Update max response time
        maxResponseTime.compute(key, (k, v) -> {
            if (v == null) return new AtomicLong(responseTimeMs);
            v.updateAndGet(current -> Math.max(current, responseTimeMs));
            return v;
        });
        
        // Update min response time
        minResponseTime.compute(key, (k, v) -> {
            if (v == null) return new AtomicLong(responseTimeMs);
            v.updateAndGet(current -> Math.min(current, responseTimeMs));
            return v;
        });
        
        // Log performance metrics
        performanceLogger.info("PERFORMANCE_METRIC: operation={}, provider={}, model={}, responseTime={}ms", 
                operation, provider, model, responseTimeMs);
        
        // Log slow requests
        if (responseTimeMs > 5000) { // 5 seconds
            logger.warn("SLOW_REQUEST: operation={}, provider={}, model={}, responseTime={}ms", 
                    operation, provider, model, responseTimeMs);
        }
    }
    
    /**
     * Get performance statistics for an operation
     */
    public PerformanceStats getPerformanceStats(String operation, String provider, String model) {
        String key = provider + "_" + model + "_" + operation;
        
        long requestCount = requestCounters.getOrDefault(key, new AtomicLong(0)).get();
        long totalTime = totalResponseTime.getOrDefault(key, new AtomicLong(0)).get();
        long maxTime = maxResponseTime.getOrDefault(key, new AtomicLong(0)).get();
        long minTime = minResponseTime.getOrDefault(key, new AtomicLong(Long.MAX_VALUE)).get();
        
        double avgTime = requestCount > 0 ? (double) totalTime / requestCount : 0;
        
        return new PerformanceStats(
                operation, provider, model, requestCount, 
                avgTime, maxTime, minTime == Long.MAX_VALUE ? 0 : minTime
        );
    }
    
    /**
     * Get all performance statistics
     */
    public java.util.Map<String, PerformanceStats> getAllPerformanceStats() {
        java.util.Map<String, PerformanceStats> stats = new ConcurrentHashMap<>();
        
        requestCounters.keySet().forEach(key -> {
            String[] parts = key.split("_", 3);
            if (parts.length >= 3) {
                String provider = parts[0];
                String model = parts[1];
                String operation = parts[2];
                stats.put(key, getPerformanceStats(operation, provider, model));
            }
        });
        
        return stats;
    }
    
    /**
     * Reset all performance statistics
     */
    public void resetStats() {
        requestCounters.clear();
        totalResponseTime.clear();
        maxResponseTime.clear();
        minResponseTime.clear();
        logger.info("Performance statistics reset");
    }
    
    /**
     * Performance statistics data class
     */
    public static class PerformanceStats {
        private final String operation;
        private final String provider;
        private final String model;
        private final long requestCount;
        private final double avgResponseTime;
        private final long maxResponseTime;
        private final long minResponseTime;
        
        public PerformanceStats(String operation, String provider, String model, 
                              long requestCount, double avgResponseTime, 
                              long maxResponseTime, long minResponseTime) {
            this.operation = operation;
            this.provider = provider;
            this.model = model;
            this.requestCount = requestCount;
            this.avgResponseTime = avgResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.minResponseTime = minResponseTime;
        }
        
        // Getters
        public String getOperation() { return operation; }
        public String getProvider() { return provider; }
        public String getModel() { return model; }
        public long getRequestCount() { return requestCount; }
        public double getAvgResponseTime() { return avgResponseTime; }
        public long getMaxResponseTime() { return maxResponseTime; }
        public long getMinResponseTime() { return minResponseTime; }
    }
}
