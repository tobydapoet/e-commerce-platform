package com.example.e_commerce.repository;

import com.example.e_commerce.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    boolean existsByAttributeId(Long attributeId);
    List<AttributeValue> findByAttributeId(Long attributeValueId);
}
