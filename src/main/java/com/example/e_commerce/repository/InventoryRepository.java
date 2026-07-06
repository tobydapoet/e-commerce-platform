package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Query("""
        SELECT i
        FROM Inventory i
        WHERE :keyword IS NULL
           OR :keyword = ''
           OR LOWER(i.productVariant.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(i.productVariant.product.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    Page<Inventory> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
