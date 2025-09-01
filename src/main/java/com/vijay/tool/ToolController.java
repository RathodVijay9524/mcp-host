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

    record ToolHelp(String name, String description, String example) {}

    @GetMapping
    public List<ToolHelp> listToolsWithExamples() {
        return Arrays.stream(toolProvider.getToolCallbacks())
                .map(cb -> {
                    var def = cb.getToolDefinition();
                    String name = ToolUtils.cleanToolName(def.name());
                    String desc = def.description();
                    String example = ToolUtils.generateExample(name, def.inputSchema());
                    return new ToolHelp(name, desc, example);
                })
                .toList();
    }
}
