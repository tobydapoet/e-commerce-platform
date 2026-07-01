package com.example.e_commerce.dto.response;

import com.example.e_commerce.constant.StoreStatus;

import java.time.LocalDateTime;

public record StoreRes (
    Long id,
    UserSimpleRes owner,
    String name,
    String description,
    String logo,
    String banner,
    String phone,
    String email,
    StoreStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
){}