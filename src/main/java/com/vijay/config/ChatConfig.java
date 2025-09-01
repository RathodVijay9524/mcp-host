package com.vijay.config;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Configuration
class ChatConfig {

    private static final Logger logger = LoggerFactory.getLogger(ChatConfig.class);


    // Merge all MCP servers
    @Bean
    @Primary
    public ToolCallbackProvider mcpToolCallbackProvider(List<McpSyncClient> mcpSyncClients) {
        return new LoggingMcpToolCallbackProvider(mcpSyncClients);
    }




    // Gemini client (cloud)
    @Bean
    ChatMemory chatMemory() {
        // Keeps the last N messages per conversationId (in-memory only)
        return MessageWindowChatMemory.builder()
                .maxMessages(20) // tune as needed
                .build();
    }

    @Primary
    @Bean(name = "geminiClient")
    ChatClient geminiClient(OpenAiChatModel openAiChatModel,
                            ToolCallbackProvider mcp, ChatMemory chatMemory) {

        var opts = OpenAiChatOptions.builder()
                .toolChoice("auto")
                .build();

        logger.info("Creating Gemini Chat Client");

        return ChatClient.builder(openAiChatModel)
                .defaultOptions(opts)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultToolCallbacks(mcp.getToolCallbacks())
                .build();
    }

    @Bean(name = "ollamaClient")
    ChatClient ollamaClient(OllamaChatModel ollamaChatModel,
                            ToolCallbackProvider mcp, ChatMemory chatMemory) {

        logger.info("Creating Ollama Chat Client");

        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultToolCallbacks(mcp.getToolCallbacks())
                .build();
    }
}
