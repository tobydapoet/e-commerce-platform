package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    List<Attribute> findByProductId(Long productId);
}
