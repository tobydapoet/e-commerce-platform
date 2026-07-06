package com.example.e_commerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean active = false;

    private String image;

    @OneToMany(mappedBy = "productVariant")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<CartItem> cartItems = new HashSet<>();

    @OneToMany(mappedBy = "productVariant")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<OrderItem> orderItems = new HashSet<>();

    @OneToOne(
            mappedBy = "productVariant",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Inventory inventory;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<ProductVariantAttributeValue> variantAttributeValues = new HashSet<>();
}
