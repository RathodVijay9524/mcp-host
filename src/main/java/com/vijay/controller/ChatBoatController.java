package com.vijay.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
class ChatBoatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatBoatController.class);

    private final ChatClient gemini; // from ChatConfig
    private final ChatClient ollama; // from ChatConfig
    private final String systemPrompt; // <-- now dynamic

    ChatBoatController(@Qualifier("geminiClient") ChatClient gemini,
                       @Qualifier("ollamaClient") ChatClient ollama,
                       ToolCallbackProvider toolProvider) {   // ✅ inject toolProvider
        this.gemini = gemini;
        this.ollama = ollama;

        // Load system prompt template
        PromptTemplate template = new PromptTemplate(new ClassPathResource("prompts/tool-only.st"));

        // Build dynamic tool list
        String toolList = Arrays.stream(toolProvider.getToolCallbacks())
                .map(cb -> {
                    String name = cleanToolName(cb.getToolDefinition().name());
                    return "- " + name + " → " + cb.getToolDefinition().description();
                })
                .collect(Collectors.joining("\n"));

        // Render system prompt with tool list injected
        this.systemPrompt = template.render(Map.of("toolList", toolList));
    }

    private String cleanToolName(String fullName) {
        int lastUnderscore = fullName.lastIndexOf("_");
        return (lastUnderscore >= 0) ? fullName.substring(lastUnderscore + 1) : fullName;
    }


    // --- DTOs ---
    record ChatRequest(
            String message,
            String provider,
            String model,
            String apiKey,
            String baseUrl,
            String conversationId
    ) {}

    record ChatResponse(
            String provider,
            String model,
            String answer
    ) {}

    // { "message": "Create a note titled Hello with body This is my first note.", "provider": "ollama" }
    // { "message": "List sample FAQs for the product.", "provider": "gemini" }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest req,
                                             HttpServletRequest http) {

        String provider = (req.provider() == null) ? "gemini" : req.provider().toLowerCase();
        String model = req.model();
        String apiKey = req.apiKey();
        String baseUrl = req.baseUrl();

        logger.info("Received request with provider: {}, model: {}, apiKey: {}, baseUrl: {}",
                provider, model, apiKey, baseUrl);

        ChatClient client;

        // --- Choose client ---
        if ("ollama".equals(provider)) {
            logger.info("Using Ollama client");
            client = ollama;

        } else if (apiKey != null && !apiKey.isBlank()) {
            logger.info("Dynamically building OpenAI client with API key and base URL: {}", baseUrl);
            // Dynamically build a new client with supplied baseUrl + apiKey
            String url = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : "https://api.openai.com/v1"; // safe default

            OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl(url)
                    .apiKey(apiKey)
                    .build();

           /*OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .build();*/

            client = gemini;

        } else {
            // Fallback to default Gemini bean (from ChatConfig)
            logger.info("Falling back to default Gemini client");
            client = gemini;
        }

        // --- Conversation memory ---
        String conversationId = (req.conversationId() != null && !req.conversationId().isBlank())
                ? req.conversationId()
                : http.getSession(true).getId();

        var prompt = client.prompt()
                .system(systemPrompt)
                .user(req.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId));

        // --- Model override ---
        if (model != null && !model.isBlank()) {
            if ("ollama".equals(provider)) {
            } else {
                prompt = prompt.options(OpenAiChatOptions.builder()
                        .model(model)
                        .build());
            }
        }

        // --- Execute ---
        try {
            String answer = prompt.call().content();
            logger.info("Received answer: {}", answer);

            return ResponseEntity.ok(
                    new ChatResponse(provider, (model == null ? "" : model), answer)
            );
        } catch (Exception e) {
            logger.error("Error during request execution: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ChatResponse("error", "", "Internal Server Error"));
        }
    }
}
