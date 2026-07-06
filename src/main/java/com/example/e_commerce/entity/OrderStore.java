package com.example.e_commerce.entity;

import com.example.e_commerce.constant.OrderStatus;
import com.example.e_commerce.constant.OrderStoreStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "order_stores")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_coupon_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Coupon storeCoupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrderStoreStatus status;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2, name = "shipping_fee")
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 500)
    private String note;

    @OneToMany(
            mappedBy = "orderStore",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<OrderItem> orderItems = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}
