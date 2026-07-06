package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class UpdateProductReq {
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    String name;

    String description;

    Long categoryId;

    MultipartFile thumbnail;
}
