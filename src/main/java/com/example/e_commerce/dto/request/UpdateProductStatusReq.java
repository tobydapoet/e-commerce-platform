package com.example.e_commerce.dto.request;

import com.example.e_commerce.constant.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductStatusReq {
    @NotNull(message = "Status must not be null")
    private ProductStatus status;
}