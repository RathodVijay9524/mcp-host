package com.vijay.tool;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/tools")
public class ToolController {

    private final ToolCallbackProvider toolProvider;

    public ToolController(ToolCallbackProvider toolProvider) {
        this.toolProvider = toolProvider;
    }

    record ToolHelp(String name, String description) {}

    @GetMapping
    public List<ToolHelp> listTools() {
        return Arrays.stream(toolProvider.getToolCallbacks())
                .map(cb -> new ToolHelp(
                        cleanToolName(cb.getToolDefinition().name()),
                        cb.getToolDefinition().description()
                ))
                .toList();
    }

    private String cleanToolName(String fullName) {
        int lastUnderscore = fullName.lastIndexOf("_");
        return (lastUnderscore >= 0) ? fullName.substring(lastUnderscore + 1) : fullName;
    }
}
