package com.example.e_commerce.dto.response;

import java.time.LocalDateTime;

public record AddressRes(
        Long id,
        String name,
        UserSimpleRes user,
        String phone,
        String address,
        Boolean isDefault,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
