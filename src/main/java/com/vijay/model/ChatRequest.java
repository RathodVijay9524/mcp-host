package com.vijay.model;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatRequest {
    
    @NotBlank(message = "Message cannot be blank")
    @Size(max = 4000, message = "Message cannot exceed 4000 characters")
    private String message;
    
    @Size(max = 50, message = "Provider name cannot exceed 50 characters")
    private String provider;
    
    @Size(max = 100, message = "Model name cannot exceed 100 characters")
    private String model;
    
    @Size(max = 500, message = "API key cannot exceed 500 characters")
    private String apiKey;
    
    @Size(max = 500, message = "Base URL cannot exceed 500 characters")
    private String baseUrl;
    
    @Size(max = 100, message = "Conversation ID cannot exceed 100 characters")
    private String conversationId;
}
