package com.vijay.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
class ChatBoatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatBoatController.class);

    private final ChatClient gemini; // from ChatConfig
    private final ChatClient ollama; // from ChatConfig

    ChatBoatController(@Qualifier("geminiClient") ChatClient gemini,
                       @Qualifier("ollamaClient") ChatClient ollama) {
        this.gemini = gemini;
        this.ollama = ollama;
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
                .system("""
                    You have MCP tools available:
                    - createNote(title, body)
                    - listFaqs()
                    Prefer calling these tools when relevant.
                    """)
                .user(req.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId));

        // --- Model override ---
        if (model != null && !model.isBlank()) {
            if ("ollama".equals(provider)) {
                prompt = prompt.options(ChatOptions.builder()
                        .model(model)
                        .build());
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
