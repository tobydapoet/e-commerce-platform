package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAttributeValueReq {
    @NotBlank(message = "Value must not be blank")
    @Size(max = 100, message = "Value must not exceed 100 characters")
    private String value;
}
