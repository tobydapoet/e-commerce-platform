package com.example.e_commerce.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAddressReq {
    @Size(max = 100)
    private String name;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String address;
}
