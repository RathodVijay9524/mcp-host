package com.vijay.controller;

import com.vijay.service.CachedToolService;
import com.vijay.service.RedisChatMemoryService;
import com.vijay.service.PerformanceMonitoringService;
import com.vijay.service.MetricsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Performance monitoring controller
 */
@RestController
@RequestMapping("/api/performance")
@ConditionalOnBean(RedisChatMemoryService.class)
public class PerformanceController {
    
        private final CacheManager cacheManager;
    private final CachedToolService cachedToolService;
    private final RedisChatMemoryService redisChatMemoryService;
    private final PerformanceMonitoringService performanceMonitoringService;
    private final MetricsService metricsService;
    
    public PerformanceController(CacheManager cacheManager, 
                               CachedToolService cachedToolService,
                               RedisChatMemoryService redisChatMemoryService,
                               PerformanceMonitoringService performanceMonitoringService,
                               MetricsService metricsService) {
        this.cacheManager = cacheManager;
        this.cachedToolService = cachedToolService;
        this.redisChatMemoryService = redisChatMemoryService;
        this.performanceMonitoringService = performanceMonitoringService;
        this.metricsService = metricsService;
    }
    
    @GetMapping("/cache-stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Map<String, Object> cacheStats = new HashMap<>();
                cacheStats.put("name", cacheName);
                cacheStats.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
                stats.put(cacheName, cacheStats);
            }
        });
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/tool-stats")
    public ResponseEntity<Map<String, Object>> getToolStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            int toolCount = cachedToolService.getToolCount();
            stats.put("totalTools", toolCount);
            stats.put("cached", true);
        } catch (Exception e) {
            stats.put("error", e.getMessage());
            stats.put("cached", false);
        }
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/memory-stats")
    public ResponseEntity<Map<String, Object>> getMemoryStats() {
        Map<String, Object> stats = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        stats.put("totalMemory", runtime.totalMemory());
        stats.put("freeMemory", runtime.freeMemory());
        stats.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        stats.put("maxMemory", runtime.maxMemory());
        stats.put("availableProcessors", runtime.availableProcessors());
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get detailed performance statistics
     */
    @GetMapping("/detailed-stats")
    public ResponseEntity<Map<String, Object>> getDetailedStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Performance monitoring stats
        Map<String, PerformanceMonitoringService.PerformanceStats> perfStats = 
                performanceMonitoringService.getAllPerformanceStats();
        stats.put("performanceStats", perfStats);
        
        // Tool service stats
        stats.put("toolCount", cachedToolService.getToolCount());
        stats.put("toolListGenerationTime", cachedToolService.getLastGenerationTime());
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Reset performance statistics
     */
    @GetMapping("/reset-stats")
    public ResponseEntity<Map<String, String>> resetStats() {
        performanceMonitoringService.resetStats();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Performance statistics reset successfully");
        return ResponseEntity.ok(response);
    }
}
