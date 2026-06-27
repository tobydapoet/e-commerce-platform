package com.example.e_commerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class SessionReq {
    private UUID userId;

    private String token;

    private LocalDateTime expiresAt;
}
