package com.vijay.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring application and circuit breaker states
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    public HealthController(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        // Add circuit breaker states
        Map<String, Object> circuitBreakers = new HashMap<>();
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            Map<String, Object> cbState = new HashMap<>();
            cbState.put("state", cb.getState().toString());
            cbState.put("failureRate", cb.getMetrics().getFailureRate());
            cbState.put("numberOfBufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
            cbState.put("numberOfFailedCalls", cb.getMetrics().getNumberOfFailedCalls());
            cbState.put("numberOfSuccessfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
            circuitBreakers.put(cb.getName(), cbState);
        });
        
        health.put("circuitBreakers", circuitBreakers);
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/circuit-breakers")
    public ResponseEntity<Map<String, Object>> circuitBreakerStates() {
        Map<String, Object> states = new HashMap<>();
        
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            Map<String, Object> cbInfo = new HashMap<>();
            cbInfo.put("state", cb.getState().toString());
            cbInfo.put("failureRate", cb.getMetrics().getFailureRate());
            cbInfo.put("numberOfBufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
            cbInfo.put("numberOfFailedCalls", cb.getMetrics().getNumberOfFailedCalls());
            cbInfo.put("numberOfSuccessfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
            states.put(cb.getName(), cbInfo);
        });
        
        return ResponseEntity.ok(states);
    }
}
