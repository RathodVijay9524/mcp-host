package com.vijay.config;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.definition.ToolDefinition;

public interface ToolCallback {
    ToolDefinition getToolDefinition();
    String call(String toolInput);
    String call(String toolInput, ToolContext toolContext);
}

