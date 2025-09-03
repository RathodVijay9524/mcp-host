package com.vijay.controller;

import com.vijay.model.ChatRequest;
import com.vijay.model.ChatResponse;
import com.vijay.service.ResilientChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
class ChatBoatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatBoatController.class);

    private final ResilientChatService resilientChatService;

    ChatBoatController(ResilientChatService resilientChatService) {
        this.resilientChatService = resilientChatService;
    }


    // Note: Using model classes from com.vijay.model package instead of local records

    // { "message": "Create a note titled Hello with body This is my first note.", "provider": "ollama" }
    // { "message": "List sample FAQs for the product.", "provider": "gemini" }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest req,
                                             HttpServletRequest http) {
        
        logger.info("Received chat request with provider: {}, model: {}, hasApiKey: {}, hasBaseUrl: {}",
                req.getProvider(), req.getModel(), 
                req.getApiKey() != null && !req.getApiKey().isBlank(),
                req.getBaseUrl() != null && !req.getBaseUrl().isBlank());

        try {
            // Generate conversation ID
            String conversationId = (req.getConversationId() != null && !req.getConversationId().isBlank())
                    ? req.getConversationId()
                    : http.getSession(true).getId();

            // Process the chat request through the resilient service layer
            ChatResponse response = resilientChatService.processChatRequest(req, conversationId);
            
            logger.info("Successfully processed chat request for conversation: {}", conversationId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing chat request: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ChatResponse("error", "", "Internal Server Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/providers")
    public ResponseEntity<String[]> getAvailableProviders() {
        try {
            String[] providers = resilientChatService.getAvailableProviders();
            return ResponseEntity.ok(providers);
        } catch (Exception e) {
            logger.error("Error getting available providers: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new String[0]);
        }
    }
    
    @GetMapping("/models")
    public ResponseEntity<String[]> getAvailableModels(@RequestParam(required = false) String provider) {
        try {
            String[] models = resilientChatService.getAvailableModels(provider);
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            logger.error("Error getting available models for provider {}: {}", provider, e.getMessage(), e);
            return ResponseEntity.status(500).body(new String[0]);
        }
    }
}
