package com.example.e_commerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "product_variant_attribute_values",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_variant_attribute_value",
                columnNames = {"product_variant_id", "attribute_value_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantAttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne
    @JoinColumn(name = "attribute_value_id", nullable = false)
    private AttributeValue attributeValue;
}
