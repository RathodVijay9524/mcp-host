package com.vijay.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing application metrics
 */
@Service
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> providerCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> providerTimers = new ConcurrentHashMap<>();
    
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * Record a chat request
     */
    public void recordChatRequest(String provider, String model) {
        Counter counter = providerCounters.computeIfAbsent(
            provider + "_" + model,
            key -> Counter.builder("chat.requests.by_provider")
                    .description("Chat requests by provider and model")
                    .tag("provider", provider)
                    .tag("model", model)
                    .register(meterRegistry)
        );
        counter.increment();
    }
    
    /**
     * Record a successful chat response
     */
    public void recordChatSuccess(String provider, String model) {
        Counter counter = meterRegistry.counter("chat.responses.success.by_provider",
                "provider", provider, "model", model);
        counter.increment();
    }
    
    /**
     * Record a failed chat response
     */
    public void recordChatError(String provider, String model, String errorType) {
        Counter counter = meterRegistry.counter("chat.responses.error.by_provider",
                "provider", provider, "model", model, "error_type", errorType);
        counter.increment();
    }
    
    /**
     * Record chat response time
     */
    public Timer.Sample startChatTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Stop chat timer and record the duration
     */
    public void recordChatResponseTime(Timer.Sample sample, String provider, String model) {
        Timer timer = providerTimers.computeIfAbsent(
            provider + "_" + model,
            key -> Timer.builder("chat.response.time.by_provider")
                    .description("Chat response time by provider and model")
                    .tag("provider", provider)
                    .tag("model", model)
                    .register(meterRegistry)
        );
        sample.stop(timer);
    }
    
    /**
     * Record MCP tool call
     */
    public void recordMcpToolCall(String toolName, String serverName) {
        Counter counter = meterRegistry.counter("mcp.tool.calls.by_tool",
                "tool_name", toolName, "server_name", serverName);
        counter.increment();
    }
    
    /**
     * Record circuit breaker state change
     */
    public void recordCircuitBreakerStateChange(String instance, String fromState, String toState) {
        Counter counter = meterRegistry.counter("resilience.circuit.breaker.state.change",
                "instance", instance, "from_state", fromState, "to_state", toState);
        counter.increment();
    }
    
    /**
     * Record cache hit
     */
    public void recordCacheHit(String cacheName) {
        Counter counter = meterRegistry.counter("cache.hits.by_name",
                "cache_name", cacheName);
        counter.increment();
    }
    
    /**
     * Record cache miss
     */
    public void recordCacheMiss(String cacheName) {
        Counter counter = meterRegistry.counter("cache.misses.by_name",
                "cache_name", cacheName);
        counter.increment();
    }
    
    /**
     * Record custom business metric
     */
    public void recordCustomMetric(String metricName, String... tags) {
        Counter counter = meterRegistry.counter(metricName, tags);
        counter.increment();
    }
}
