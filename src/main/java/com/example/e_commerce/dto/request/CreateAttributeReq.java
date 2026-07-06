package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateAttributeReq {
    @NotEmpty(message = "Attribute names must not be empty")
    private List<@NotBlank(message = "Attribute name must not be blank") String> names;
}