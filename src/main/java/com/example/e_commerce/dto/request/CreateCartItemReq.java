package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CreateCartItemReq {
    @NotNull(message = "productVariantId is required.")
    Long productVariantId;

    @NotNull(message = "quantity is required.")
    Integer quantity;
}
