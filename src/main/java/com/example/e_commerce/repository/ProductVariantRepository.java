package com.example.e_commerce.repository;

import com.example.e_commerce.dto.response.ProductPriceRangeProjection;
import com.example.e_commerce.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Query("""
            SELECT pv
            FROM ProductVariant pv
            JOIN ProductVariantAttributeValue pvav
                on pv.id = pvav.productVariant.id
            WHERE pvav.attributeValue.id IN :attributeValueIds
            AND pv.active = true
            GROUP BY pv
            HAVING COUNT(DISTINCT pvav.attributeValue.id) =:size
            """)
    Optional<ProductVariant> findByAttributeValueIds(
            @Param("attributeValueIds") List<Long> attributeValueIds,
            @Param("size") long size
    );

    @Query("""
    SELECT v.product.id AS productId,
           MIN(v.price) AS minPrice,
           MAX(v.price) AS maxPrice
    FROM ProductVariant v
    WHERE v.product.id IN :productIds
    GROUP BY v.product.id
""")
    List<ProductPriceRangeProjection> findPriceRangeByProductIds(@Param("productIds") List<Long> productIds);
}
