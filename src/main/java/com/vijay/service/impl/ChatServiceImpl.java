package com.vijay.service.impl;

import com.vijay.exception.AIProviderException;
import com.vijay.exception.ChatServiceException;
import com.vijay.exception.ValidationException;
import com.vijay.model.ChatRequest;
import com.vijay.model.ChatResponse;
import com.vijay.service.AIClientService;
import com.vijay.service.ChatService;
import com.vijay.service.MetricsService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of ChatService for processing chat requests
 */
@Service
public class ChatServiceImpl implements ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);
    
    private final AIClientService aiClientService;
    private final String systemPrompt;
    private final MetricsService metricsService;
    
    public ChatServiceImpl(AIClientService aiClientService, String systemPrompt, MetricsService metricsService) {
        this.aiClientService = aiClientService;
        this.systemPrompt = systemPrompt;
        this.metricsService = metricsService;
    }
    
    @Override
    public ChatResponse processChatRequest(ChatRequest request, String conversationId) {
        logger.info("Processing chat request for conversation: {}", conversationId);
        
        // Record metrics
        String provider = request.getProvider() != null ? request.getProvider() : aiClientService.getDefaultProvider();
        String model = request.getModel() != null ? request.getModel() : "";
        metricsService.recordChatRequest(provider, model);
        var timerSample = metricsService.startChatTimer();
        
        // Validate request
        validateChatRequest(request);
        
        try {
            // Get the appropriate ChatClient
            ChatClient client = aiClientService.getChatClient(
                request.getProvider(), 
                request.getModel(), 
                request.getApiKey(), 
                request.getBaseUrl()
            );
            
            // Build the prompt with system message and user input
            var promptBuilder = client.prompt()
                    .system(systemPrompt)
                    .user(request.getMessage())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId));
            
            // Apply model-specific options if needed
            if (request.getModel() != null && !request.getModel().isBlank() && 
                !"ollama".equals(request.getProvider())) {
                promptBuilder = promptBuilder.options(OpenAiChatOptions.builder()
                        .model(request.getModel())
                        .build());
            }
            
            // Execute the chat request
            String answer = promptBuilder.call().content();
            logger.info("Successfully generated response for conversation: {}", conversationId);
            
            // Record success metrics
            metricsService.recordChatSuccess(provider, model);
            metricsService.recordChatResponseTime(timerSample, provider, model);
            
            return new ChatResponse(
                request.getProvider() != null ? request.getProvider() : aiClientService.getDefaultProvider(),
                request.getModel() != null ? request.getModel() : "",
                answer
            );
            
        } catch (Exception e) {
            logger.error("Error processing chat request for conversation {}: {}", conversationId, e.getMessage(), e);
            
            // Record error metrics
            String errorType = e.getClass().getSimpleName();
            metricsService.recordChatError(provider, model, errorType);
            metricsService.recordChatResponseTime(timerSample, provider, model);
            
            // Determine the type of exception and throw appropriate custom exception
            if (e.getMessage() != null && e.getMessage().contains("provider")) {
                throw new AIProviderException(
                    request.getProvider() != null ? request.getProvider() : "unknown",
                    "Failed to process request with AI provider: " + e.getMessage(),
                    e
                );
            } else {
                throw new ChatServiceException(
                    "Failed to process chat request: " + e.getMessage(),
                    e
                );
            }
        }
    }
    
    private void validateChatRequest(ChatRequest request) {
        if (request == null) {
            throw new ValidationException("Chat request cannot be null");
        }
        
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new ValidationException("message", "Message cannot be empty");
        }
        
        if (request.getMessage().length() > 4000) {
            throw new ValidationException("message", "Message cannot exceed 4000 characters");
        }
        
        if (request.getProvider() != null && !aiClientService.isProviderSupported(request.getProvider())) {
            throw new ValidationException("provider", 
                String.format("Unsupported provider '%s'. Supported providers: %s", 
                    request.getProvider(), String.join(", ", aiClientService.getSupportedProviders())));
        }
    }
    
    @Override
    public String[] getAvailableProviders() {
        return new String[]{"gemini", "ollama"};
    }
    
    @Override
    public String[] getAvailableModels(String provider) {
        if ("gemini".equals(provider)) {
            return new String[]{"gemini-1.5-flash", "gemini-1.5-pro"};
        } else if ("ollama".equals(provider)) {
            return new String[]{"qwen2.5-coder:3b", "llama3.2", "codellama"};
        }
        return new String[0];
    }
}
