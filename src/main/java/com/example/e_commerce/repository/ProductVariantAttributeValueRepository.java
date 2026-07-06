package com.example.e_commerce.repository;

import com.example.e_commerce.entity.ProductVariantAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantAttributeValueRepository extends JpaRepository<ProductVariantAttributeValue, Long> {
    boolean existsByAttributeValueId(Long attributeValueId);
}
