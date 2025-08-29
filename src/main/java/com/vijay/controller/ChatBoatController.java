package com.vijay.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

record ChatRequest(String message, String provider, String model,String conversationId) {}
record ChatResponse(String provider, String model, String answer) {}

@RestController
@RequestMapping("/api/ai")
class ChatBoatController {

    private final ChatClient gemini;
    private final ChatClient ollama;

    ChatBoatController(@Qualifier("geminiClient") ChatClient gemini,
                       @Qualifier("ollamaClient") ChatClient ollama) {
        this.gemini = gemini;
        this.ollama = ollama;
    }
    // { "message": "Create a note titled Hello with body This is my first note.", "provider": "ollama" }
    // { "message": "List sample FAQs for the product.", "provider": "gemini" }
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest req,
                                             HttpServletRequest http) {

        String provider = (req.provider() == null) ? "gemini" : req.provider().toLowerCase();
        String model = req.model();

        ChatClient client = switch (provider) {
            case "ollama" -> ollama;
            case "gemini" -> gemini;
            default -> gemini;
        };

        // choose an ID: prefer explicit from client, else use HTTP session
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
                // <<< add the memory advisor param here
                .advisors(a -> a.param(
                        ChatMemory.CONVERSATION_ID,
                        conversationId
                ));

        if ("gemini".equals(provider) && model != null && !model.isBlank()) {
            prompt = prompt.options(OpenAiChatOptions.builder()
                    .model(model)
                    .build());
        }

        String answer = prompt.call().content();
        return ResponseEntity.ok(new ChatResponse(provider, model == null ? "" : model, answer));
    }
}