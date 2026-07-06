package com.example.e_commerce.controller;

import com.example.e_commerce.dto.response.ProductVariantRes;
import com.example.e_commerce.entity.ProductVariant;
import com.example.e_commerce.mapper.ProductVariantMapper;
import com.example.e_commerce.service.AttributeValueService;
import com.example.e_commerce.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;
    private final AttributeValueService attributeValueService; // giả định đã có sẵn
    private final ProductVariantMapper mapper;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_VARIANT_READ')")
    public ResponseEntity<ProductVariantRes> findById(@PathVariable Long id) {
        ProductVariant variant = productVariantService.findById(id);
        return ResponseEntity.ok(mapper.toProductVariantRes(variant));
    }

    @GetMapping("/by-attribute-values")
    @PreAuthorize("hasAuthority('PRODUCT_VARIANT_READ')")
    public ResponseEntity<ProductVariantRes> getVariantByAttributeValues(
            @RequestParam List<Long> attributeValueIds
    ) {
        ProductVariantRes response =
                productVariantService.getVariantByAttributeValueIds(attributeValueIds);

        return ResponseEntity.ok(response);
    }
}
