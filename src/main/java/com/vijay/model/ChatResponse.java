package com.vijay.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String provider;
    private String model;
    private String answer;
}

