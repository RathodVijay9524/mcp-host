package com.vijay.service;

import com.vijay.model.ChatRequest;
import com.vijay.model.ChatResponse;

/**
 * Service interface for chat operations
 */
public interface ChatService {
    
    /**
     * Process a chat request and return a response
     * 
     * @param request the chat request containing message, provider, model, etc.
     * @param conversationId the conversation ID for maintaining context
     * @return the chat response with AI-generated answer
     */
    ChatResponse processChatRequest(ChatRequest request, String conversationId);
    
    /**
     * Get available AI providers
     * 
     * @return array of supported provider names
     */
    String[] getAvailableProviders();
    
    /**
     * Get available models for a specific provider
     * 
     * @param provider the AI provider name
     * @return array of available model names
     */
    String[] getAvailableModels(String provider);
}
