package com.example.e_commerce.dto.response;

import java.util.UUID;

public record UserSimpleRes(
        UUID id,
        String name,
        String email
) {}