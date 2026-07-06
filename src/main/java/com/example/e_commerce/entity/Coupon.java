package com.example.e_commerce.entity;

import com.example.e_commerce.constant.CouponStatus;
import com.example.e_commerce.constant.CreatorType;
import com.example.e_commerce.constant.DiscountType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false, name = "creator_type")
    private CreatorType creatorType;

    @Column(nullable = false, name = "discount_type")
    private DiscountType discountType;

    @Column(nullable = false, name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "minimum_order")
    private BigDecimal minimumOrder;

    @Column(name = "maximum_discount")
    private BigDecimal maximumDiscount;

    @Column
    private Long quantity;

    @Column(name="start_date", nullable = false)
    private LocalDate startDate;

    @Column(name="end_date", nullable = false)
    private LocalDate endDate;

    @Column()
    @Enumerated(EnumType.STRING)
    private CouponStatus status = CouponStatus.INACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Store store;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "platformCoupon", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "storeCoupon", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<OrderStore> orderStores = new HashSet<>();

    @OneToMany(mappedBy = "storeCoupon", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<CartStore> cartStores = new HashSet<>();
}
