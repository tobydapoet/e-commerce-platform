package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryReq {
    @NotBlank(message = "name is required.")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private Long parentId;
}