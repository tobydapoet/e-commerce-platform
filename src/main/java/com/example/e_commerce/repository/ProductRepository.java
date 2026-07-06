package com.example.e_commerce.repository;

import com.example.e_commerce.constant.ProductStatus;
import com.example.e_commerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
    SELECT p
    FROM Product p
    WHERE p.status NOT IN :statuses
    ORDER BY p.createdAt DESC
    """)
    Page<Product> getByPage(
            @Param("statuses") List<ProductStatus> statuses,
            Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Product p
    WHERE p.status NOT IN :statuses
      AND (:keyword IS NULL OR :keyword = '' OR
           LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
    ORDER BY p.createdAt DESC
    """)
    Page<Product> search(
            @Param("statuses") List<ProductStatus> statuses,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
