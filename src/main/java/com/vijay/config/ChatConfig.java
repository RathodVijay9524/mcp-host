package com.vijay.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class ChatConfig {

    // Gemini client (cloud)

    @Primary // optional; only if something injects a plain ChatClient
    @Bean(name = "geminiClient")
    ChatClient geminiClient(OpenAiChatModel openAiChatModel,
                            SyncMcpToolCallbackProvider mcp) {

        var opts = OpenAiChatOptions.builder()
                .toolChoice("auto")
                .build();

        return ChatClient.builder(openAiChatModel)
                .defaultOptions(opts)
                .defaultToolCallbacks(mcp.getToolCallbacks())
                .build();
    }

    @Bean(name = "ollamaClient")
    ChatClient ollamaClient(OllamaChatModel ollamaChatModel,
                            SyncMcpToolCallbackProvider mcp) {

        return ChatClient.builder(ollamaChatModel)
                .defaultToolCallbacks(mcp.getToolCallbacks())
                .build();
    }

/*
    @Bean(name = "chatClient")
    ChatClient chatClient(ChatModel chatModel,
                          SyncMcpToolCallbackProvider toolCallbackProvider) {
        return ChatClient
                .builder(chatModel)
                // 👇 make all discovered MCP tools available by default
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .build();
    }*/


}
