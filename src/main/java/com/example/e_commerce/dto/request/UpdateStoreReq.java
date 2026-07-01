package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateStoreReq {
    @Size(max = 100)
    private String name;

    private String description;

    private MultipartFile logo;

    private MultipartFile banner;
}
