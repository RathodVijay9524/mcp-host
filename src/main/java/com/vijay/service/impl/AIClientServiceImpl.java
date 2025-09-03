package com.vijay.service.impl;

import com.vijay.service.AIClientService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Implementation of AIClientService for managing AI client selection and configuration
 */
@Service
public class AIClientServiceImpl implements AIClientService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIClientServiceImpl.class);
    
    private final ChatClient geminiClient;
    private final ChatClient ollamaClient;
    private final Set<String> supportedProviders = Set.of("gemini", "ollama");
    private final String defaultProvider = "gemini";
    
    public AIClientServiceImpl(@Qualifier("geminiClient") ChatClient geminiClient,
                              @Qualifier("ollamaClient") ChatClient ollamaClient) {
        this.geminiClient = geminiClient;
        this.ollamaClient = ollamaClient;
    }
    
    @Override
    public ChatClient getChatClient(String provider, String model, String apiKey, String baseUrl) {
        String normalizedProvider = (provider == null) ? defaultProvider : provider.toLowerCase();
        
        logger.info("Getting ChatClient for provider: {}, model: {}, hasCustomApiKey: {}, hasCustomBaseUrl: {}", 
                   normalizedProvider, model, apiKey != null && !apiKey.isBlank(), baseUrl != null && !baseUrl.isBlank());
        
        switch (normalizedProvider) {
            case "ollama":
                logger.info("Using Ollama client");
                return ollamaClient;
                
            case "gemini":
                if (apiKey != null && !apiKey.isBlank()) {
                    logger.info("Dynamically building OpenAI client with custom API key and base URL: {}", baseUrl);
                    // For now, we'll use the default gemini client
                    // TODO: Implement dynamic client creation with custom API key and base URL
                    return geminiClient;
                } else {
                    logger.info("Using default Gemini client");
                    return geminiClient;
                }
                
            default:
                logger.warn("Unsupported provider '{}', falling back to default provider '{}'", 
                           normalizedProvider, defaultProvider);
                return geminiClient;
        }
    }
    
    @Override
    public boolean isProviderSupported(String provider) {
        return provider != null && supportedProviders.contains(provider.toLowerCase());
    }
    
    @Override
    public String getDefaultProvider() {
        return defaultProvider;
    }
    
    @Override
    public String[] getSupportedProviders() {
        return supportedProviders.toArray(new String[0]);
    }
}
