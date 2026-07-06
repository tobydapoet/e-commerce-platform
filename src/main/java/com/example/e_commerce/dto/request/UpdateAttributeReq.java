package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAttributeReq {
    @NotBlank(message = "Attribute name must not be blank")
    @Size(max = 100, message = "Attribute name must not exceed 100 characters")
    private String name;
}