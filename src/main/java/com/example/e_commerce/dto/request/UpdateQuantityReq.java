package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateQuantityReq {
    @NotNull(message = "Quantity must not be null")
    @Min(value = 0, message = "Quantity must not be negative")
    private Integer quantity;
}