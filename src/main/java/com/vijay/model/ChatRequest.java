package com.vijay.model;

import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatRequest {
    private String message;
    private String provider;
    private String model;
    private String conversationId;
}
