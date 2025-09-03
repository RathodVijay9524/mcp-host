package com.vijay.service;

import com.vijay.tool.ToolUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Cached service for tool-related operations
 */
@Service
public class CachedToolService {
    
    private static final Logger logger = LoggerFactory.getLogger(CachedToolService.class);
    
    private final ToolCallbackProvider toolProvider;
    private final AtomicLong lastGenerationTime = new AtomicLong(0);
    
    public CachedToolService(ToolCallbackProvider toolProvider) {
        this.toolProvider = toolProvider;
    }
    
    /**
     * Get tool definitions with caching
     */
    @Cacheable(value = "toolDefinitions", key = "'all'")
    public List<Map<String, String>> getToolDefinitions() {
        logger.info("Loading tool definitions from MCP servers (cache miss)");
        
        return Arrays.stream(toolProvider.getToolCallbacks())
                .map(cb -> {
                    var def = cb.getToolDefinition();
                    Map<String, String> toolInfo = Map.of(
                        "name", ToolUtils.cleanToolName(def.name()),
                        "description", def.description(),
                        "inputSchema", def.inputSchema(),
                        "example", ToolUtils.generateExample(ToolUtils.cleanToolName(def.name()), def.inputSchema())
                    );
                    return toolInfo;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get tool list for system prompt with caching
     */
    @Cacheable(value = "systemPrompts", key = "'toolList'")
    public String getToolListForPrompt() {
        logger.info("Generating tool list for system prompt (cache miss)");
        long startTime = System.currentTimeMillis();
        
        String result = Arrays.stream(toolProvider.getToolCallbacks())
                .map(cb -> {
                    var def = cb.getToolDefinition();
                    String name = ToolUtils.cleanToolName(def.name());
                    String desc = def.description();
                    String example = ToolUtils.generateExample(name, def.inputSchema());
                    return "- " + name + " → " + desc + "\n   Example: " + example;
                })
                .collect(Collectors.joining("\n\n"));
        
        long endTime = System.currentTimeMillis();
        lastGenerationTime.set(endTime - startTime);
        
        return result;
    }
    
    /**
     * Get tool count with caching
     */
    @Cacheable(value = "toolDefinitions", key = "'count'")
    public int getToolCount() {
        logger.info("Counting tools from MCP servers (cache miss)");
        return toolProvider.getToolCallbacks().length;
    }
    
    /**
     * Get tools by category (if we implement categorization later)
     */
    @Cacheable(value = "toolDefinitions", key = "#category")
    public List<Map<String, String>> getToolsByCategory(String category) {
        logger.info("Loading tools for category: {} (cache miss)", category);
        
        return getToolDefinitions().stream()
                .filter(tool -> tool.get("name").toLowerCase().contains(category.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get the last generation time in milliseconds
     */
    public long getLastGenerationTime() {
        return lastGenerationTime.get();
    }
}
