package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAddressReq {
    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Phone is required")
    @Size(max = 20)
    private String phone;

    @NotBlank(message = "Address is required")
    @Size(max = 255)
    private String fullAddress;
}
