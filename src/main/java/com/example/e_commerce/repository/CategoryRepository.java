package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameAndParentIsNull(String name);

    boolean existsByNameAndParent(String name, Category parent);
}
