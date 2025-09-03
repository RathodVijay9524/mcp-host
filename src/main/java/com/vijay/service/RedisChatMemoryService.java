package com.vijay.service;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based chat memory service for distributed systems
 */
@Service
@ConditionalOnClass(RedisTemplate.class)
public class RedisChatMemoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisChatMemoryService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CHAT_MEMORY_PREFIX = "chat:memory:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    
    public RedisChatMemoryService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Get or create chat memory for a conversation
     */
    @Cacheable(value = "chatMemory", key = "#conversationId")
    public ChatMemory getChatMemory(String conversationId) {
        logger.info("Creating new chat memory for conversation: {}", conversationId);
        
        // Create in-memory chat memory as fallback
        // In a full implementation, you might want to store/retrieve from Redis
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }
    
    /**
     * Store chat message in Redis
     */
    public void storeMessage(String conversationId, String role, String content) {
        String key = CHAT_MEMORY_PREFIX + conversationId + ":" + System.currentTimeMillis();
        
        ChatMessage message = new ChatMessage(role, content, System.currentTimeMillis());
        redisTemplate.opsForValue().set(key, message, DEFAULT_TTL);
        
        logger.debug("Stored message for conversation: {}", conversationId);
    }
    
    /**
     * Get recent messages for a conversation
     */
    public List<ChatMessage> getRecentMessages(String conversationId, int limit) {
        String pattern = CHAT_MEMORY_PREFIX + conversationId + ":*";
        
        // In a real implementation, you'd use Redis sorted sets or lists
        // For now, we'll return empty list as this is a simplified version
        logger.debug("Retrieving recent messages for conversation: {}", conversationId);
        return List.of();
    }
    
    /**
     * Clear chat memory for a conversation
     */
    @CacheEvict(value = "chatMemory", key = "#conversationId")
    public void clearChatMemory(String conversationId) {
        String pattern = CHAT_MEMORY_PREFIX + conversationId + ":*";
        
        // In a real implementation, you'd delete all keys matching the pattern
        logger.info("Cleared chat memory for conversation: {}", conversationId);
    }
    
    /**
     * Get conversation statistics
     */
    public ConversationStats getConversationStats(String conversationId) {
        // In a real implementation, you'd count messages in Redis
        return new ConversationStats(conversationId, 0, System.currentTimeMillis());
    }
    
    /**
     * Chat message model
     */
    public static class ChatMessage {
        private final String role;
        private final String content;
        private final long timestamp;
        
        public ChatMessage(String role, String content, long timestamp) {
            this.role = role;
            this.content = content;
            this.timestamp = timestamp;
        }
        
        public String getRole() { return role; }
        public String getContent() { return content; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Conversation statistics model
     */
    public static class ConversationStats {
        private final String conversationId;
        private final int messageCount;
        private final long lastActivity;
        
        public ConversationStats(String conversationId, int messageCount, long lastActivity) {
            this.conversationId = conversationId;
            this.messageCount = messageCount;
            this.lastActivity = lastActivity;
        }
        
        public String getConversationId() { return conversationId; }
        public int getMessageCount() { return messageCount; }
        public long getLastActivity() { return lastActivity; }
    }
}
