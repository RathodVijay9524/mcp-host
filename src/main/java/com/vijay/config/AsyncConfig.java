package com.vijay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Async configuration for the application
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * Traditional thread pool executor for backward compatibility
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncChat-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Virtual Threads executor for high concurrency
     * Java 19+ feature - perfect for I/O-bound operations like AI API calls
     * Can handle thousands of concurrent requests without blocking platform threads
     */
    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Default async executor - uses Virtual Threads for better performance
     * This will be used by @Async annotations without specifying executor
     */
    @Bean(name = "defaultAsyncExecutor")
    public Executor defaultAsyncExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
