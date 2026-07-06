package com.example.e_commerce.dto.response;

import java.time.LocalDateTime;

public record ReviewRes(
        Long id,
        UserSimpleRes user,
        Long productId,
        Long orderItemId,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}
