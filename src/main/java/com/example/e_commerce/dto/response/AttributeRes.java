package com.example.e_commerce.dto.response;

import java.util.List;

public record AttributeRes(
        Long id,
        String name,
        List<AttributeValueRes> values
) {}
