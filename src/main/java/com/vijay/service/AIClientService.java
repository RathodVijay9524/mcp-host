package com.vijay.service;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Service interface for AI client management
 */
public interface AIClientService {
    
    /**
     * Get the appropriate ChatClient based on provider and configuration
     * 
     * @param provider the AI provider (gemini, ollama, etc.)
     * @param model the specific model to use (optional)
     * @param apiKey custom API key (optional)
     * @param baseUrl custom base URL (optional)
     * @return the configured ChatClient
     */
    ChatClient getChatClient(String provider, String model, String apiKey, String baseUrl);
    
    /**
     * Check if a provider is supported
     * 
     * @param provider the provider name to check
     * @return true if provider is supported
     */
    boolean isProviderSupported(String provider);
    
    /**
     * Get the default provider
     * 
     * @return the default provider name
     */
    String getDefaultProvider();
    
    /**
     * Get all supported providers
     * 
     * @return array of supported provider names
     */
    String[] getSupportedProviders();
}
