package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateAttributeValueReq {
    @NotEmpty(message = "Values must not be empty")
    private List<@NotBlank(message = "Value must not be blank") String> values;
}
