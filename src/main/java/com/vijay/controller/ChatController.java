package com.vijay.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"})
class ChatController {

  /*  private final ChatClient chat;
   // ✅ injected bean (field)

    ChatController( @Qualifier("chatClient")ChatClient chat) {
        this.chat = chat;
    }

    record ChatRequest(String message) {}
    record ChatResponse(String answer) {}
    record ErrorResponse(String error, String detail) {}

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest req) {
        try {
            // Convert collection → array once; no local shadowing
               String answer = chat.prompt()
                    .system("""
                    You have MCP tools available: createNote(title, body) and listFaqs().
                    Always call these tools when relevant instead of guessing.
                    """)
                    .user(req.message())
                    .call()
                    .content();

            return ResponseEntity.ok(new ChatResponse(answer));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse("Upstream LLM/MCP error", e.getMessage()));
        }
    }*/
}

