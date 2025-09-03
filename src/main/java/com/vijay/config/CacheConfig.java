package com.vijay.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache configuration for the application
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Configure Redis cache manager with different TTL for different cache types
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
        
        // Tool definitions cache - longer TTL since they don't change often
        RedisCacheConfiguration toolDefinitionsConfig = defaultConfig.entryTtl(Duration.ofHours(1));
        
        // System prompts cache - longer TTL since they don't change often
        RedisCacheConfiguration systemPromptsConfig = defaultConfig.entryTtl(Duration.ofHours(1));
        
        // Chat memory cache - shorter TTL for active conversations
        RedisCacheConfiguration chatMemoryConfig = defaultConfig.entryTtl(Duration.ofMinutes(30));
        
        // Provider info cache - medium TTL
        RedisCacheConfiguration providerInfoConfig = defaultConfig.entryTtl(Duration.ofMinutes(15));
        
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("toolDefinitions", toolDefinitionsConfig);
        cacheConfigurations.put("systemPrompts", systemPromptsConfig);
        cacheConfigurations.put("chatMemory", chatMemoryConfig);
        cacheConfigurations.put("providerInfo", providerInfoConfig);
        
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
    
    /**
     * Configure RedisTemplate for general Redis operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}
