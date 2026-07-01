package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateStoreReq {
    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    private String description;

    private MultipartFile logo;

    private MultipartFile banner;

    @NotBlank(message = "Phone is required")
    @Size(max = 20)
    private String phone;

    private String email;
}
