package com.vijay.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics configuration for application monitoring
 */
@Configuration
public class MetricsConfig {
    
    /**
     * Counter for tracking total chat requests
     */
    @Bean
    public Counter chatRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.requests.total")
                .description("Total number of chat requests")
                .tag("type", "chat")
                .register(meterRegistry);
    }
    
    /**
     * Counter for tracking successful chat responses
     */
    @Bean
    public Counter chatSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.responses.success")
                .description("Number of successful chat responses")
                .tag("type", "success")
                .register(meterRegistry);
    }
    
    /**
     * Counter for tracking failed chat responses
     */
    @Bean
    public Counter chatErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("chat.responses.error")
                .description("Number of failed chat responses")
                .tag("type", "error")
                .register(meterRegistry);
    }
    
    /**
     * Timer for measuring chat response time
     */
    @Bean
    public Timer chatResponseTimer(MeterRegistry meterRegistry) {
        return Timer.builder("chat.response.time")
                .description("Time taken to process chat requests")
                .register(meterRegistry);
    }
    
    /**
     * Counter for tracking MCP tool calls
     */
    @Bean
    public Counter mcpToolCallCounter(MeterRegistry meterRegistry) {
        return Counter.builder("mcp.tool.calls.total")
                .description("Total number of MCP tool calls")
                .tag("type", "tool_call")
                .register(meterRegistry);
    }
    
    /**
     * Counter for tracking circuit breaker state changes
     */
    @Bean
    public Counter circuitBreakerStateCounter(MeterRegistry meterRegistry) {
        return Counter.builder("resilience.circuit.breaker.state")
                .description("Circuit breaker state changes")
                .tag("type", "state_change")
                .register(meterRegistry);
    }
    
    /**
     * Counter for tracking cache hits and misses
     */
    @Bean
    public Counter cacheHitCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cache.hits")
                .description("Number of cache hits")
                .tag("type", "hit")
                .register(meterRegistry);
    }
    
    @Bean
    public Counter cacheMissCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cache.misses")
                .description("Number of cache misses")
                .tag("type", "miss")
                .register(meterRegistry);
    }
}
