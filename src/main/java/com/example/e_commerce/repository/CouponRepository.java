package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Coupon;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends CrudRepository<Coupon, Long> {
    @Query("""
        SELECT CASE WHEN
            EXISTS (SELECT 1 FROM Order o WHERE o.platformCoupon.id = :couponId)
            OR EXISTS (SELECT 1 FROM OrderStore os WHERE os.storeCoupon.id = :couponId)
        THEN true ELSE false END
        """)
    boolean isCouponUsed(@Param("couponId") Long couponId);
}
