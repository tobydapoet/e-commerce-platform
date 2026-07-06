package com.example.e_commerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "attribute_values",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attribute_value",
                columnNames = {"attribute_id", "attribute_value"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Attribute attribute;

    @Column(name = "attribute_value", nullable = false)
    private String value;

    @OneToMany(mappedBy = "attributeValue")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<ProductVariantAttributeValue> variantAttributeValues = new HashSet<>();
}
