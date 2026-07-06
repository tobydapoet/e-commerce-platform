package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateWishlistReq {
    @NotNull(message = "ProductId is required")
    private Long productId;
}
