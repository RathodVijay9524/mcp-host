package com.vijay.config;

import io.modelcontextprotocol.client.McpSyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.List;


public class LoggingMcpToolCallbackProvider implements ToolCallbackProvider {

    private static final Logger logger = LoggerFactory.getLogger(LoggingMcpToolCallbackProvider.class);
    private final List<McpSyncClient> clients;

    public LoggingMcpToolCallbackProvider(List<McpSyncClient> clients) {
        this.clients = clients;
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        return clients.stream()
                .flatMap(client -> {
                    String serverName = client.getServerInfo().name();
                    SyncMcpToolCallbackProvider provider = new SyncMcpToolCallbackProvider(client);

                    return List.of(provider.getToolCallbacks()).stream()
                            .map(cb -> new ToolCallback() {
                                @Override
                                public ToolDefinition getToolDefinition() {
                                    return cb.getToolDefinition();
                                }

                                @Override
                                public String call(String toolInput) {
                                    logger.info("🔧 Tool '{}' from server '{}' invoked with input: {}",
                                            getToolDefinition().name(), serverName, toolInput);
                                    String result = cb.call(toolInput);
                                    logger.info("✅ Tool '{}' from server '{}' returned: {}",
                                            getToolDefinition().name(), serverName, result);
                                    return result;
                                }

                                @Override
                                public String call(String toolInput, ToolContext toolContext) {
                                    logger.info("🔧 Tool '{}' from server '{}' invoked with input: {} (context={})",
                                            getToolDefinition().name(), serverName, toolInput, toolContext);
                                    String result = cb.call(toolInput, toolContext);
                                    logger.info("✅ Tool '{}' from server '{}' returned: {}",
                                            getToolDefinition().name(), serverName, result);
                                    return result;
                                }
                            });
                })
                .toArray(ToolCallback[]::new);
    }
}