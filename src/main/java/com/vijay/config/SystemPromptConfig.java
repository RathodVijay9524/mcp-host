package com.vijay.config;

import com.vijay.service.CachedToolService;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Configuration for system prompt generation
 */
@Configuration
public class SystemPromptConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemPromptConfig.class);
    
    @Bean
    public String systemPrompt(CachedToolService cachedToolService) {
        try {
            // Load system prompt template
            PromptTemplate template = new PromptTemplate(new ClassPathResource("prompts/tool-only.st"));
            
            // Get tool list from cached service
            String toolList = cachedToolService.getToolListForPrompt();
            
            String prompt = template.render(Map.of("toolList", toolList));
            logger.info("Generated system prompt with {} tools", cachedToolService.getToolCount());
            
            return prompt;
            
        } catch (Exception e) {
            logger.error("Failed to generate system prompt", e);
            // Fallback to basic prompt
            return "You are an AI coding assistant. Use the available tools when appropriate.";
        }
    }
}
