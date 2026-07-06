package com.example.e_commerce.service;

import com.example.e_commerce.entity.AttributeValue;
import com.example.e_commerce.entity.ProductVariant;
import com.example.e_commerce.entity.ProductVariantAttributeValue;
import com.example.e_commerce.repository.ProductVariantAttributeValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantAttributeValueService {
    private final ProductVariantAttributeValueRepository attributeValueRepo;

    public ProductVariantAttributeValue create(ProductVariant productVariant, AttributeValue attributeValue) {
        ProductVariantAttributeValue productVariantAttributeValue = new ProductVariantAttributeValue();
        productVariantAttributeValue.setProductVariant(productVariant);
        productVariantAttributeValue.setAttributeValue(attributeValue);
        return attributeValueRepo.save(productVariantAttributeValue);
    }

    public List<ProductVariantAttributeValue> createBatch(List<ProductVariantAttributeValue> items) {
        return attributeValueRepo.saveAll(items);
    }
}
