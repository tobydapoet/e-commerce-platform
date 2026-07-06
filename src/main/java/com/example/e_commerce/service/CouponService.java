package com.example.e_commerce.service;

import com.example.e_commerce.constant.CouponStatus;
import com.example.e_commerce.constant.CreatorType;
import com.example.e_commerce.constant.DiscountType;
import com.example.e_commerce.constant.RoleType;
import com.example.e_commerce.dto.request.CreateCouponReq;
import com.example.e_commerce.dto.request.UpdateUnusedCouponReq;
import com.example.e_commerce.dto.request.UpdateUsedCouponReq;
import com.example.e_commerce.entity.Coupon;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepo;

    public Coupon create(CreateCouponReq req, CreatorType creator) {
        Coupon coupon = new Coupon();
        coupon.setCode(req.getCode());
        coupon.setStartDate(req.getStartDate());
        coupon.setEndDate(req.getEndDate());
        coupon.setCreatorType(creator);
        coupon.setDiscountValue(req.getDiscountValue());
        coupon.setQuantity(req.getQuantity());
        coupon.setDiscountType(req.getDiscountType());
        if(req.getMinimumOrder() != null) {
            coupon.setMaximumDiscount(req.getMaximumDiscount());
        }
        if(req.getMinimumOrder() != null) {
            coupon.setMinimumOrder(req.getMinimumOrder());
        }
        return couponRepo.save(coupon);
    }

    public Coupon findById(Long id) {
        return couponRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found."));
    }

    public Coupon updateStatus(User currentUser, Long id, CouponStatus status) {
        Coupon coupon = findById(id);
        boolean isAdmin = currentUser.getUserRoles().stream()
                .anyMatch(user -> user.getRole().getRoleName() == RoleType.ADMIN);
        boolean isSeller = currentUser.getUserRoles().stream()
                .anyMatch(user -> user.getRole().getRoleName() == RoleType.SELLER);
        if(isAdmin && coupon.getCreatorType() == CreatorType.STORE) {
            throw new ForbiddenException("You are not allowed to change the coupon status.");
        }
        if(isSeller && coupon.getCreatorType() == CreatorType.STORE) {
            throw new ForbiddenException("You are not allowed to change the coupon status.");
        }
        coupon.setStatus(status);
        return couponRepo.save(coupon);
    }

    private void validateCoupon(Coupon coupon) {
        if (coupon.getEndDate().isBefore(coupon.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            if (coupon.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Percentage discount cannot exceed 100%");
            }

            if (coupon.getMaximumDiscount() == null) {
                throw new BadRequestException("Maximum discount is required for percentage coupon");
            }
        }

        if (coupon.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            coupon.setMaximumDiscount(null);
        }

        if (coupon.getDiscountType() == DiscountType.FREE_SHIPPING) {
            coupon.setDiscountValue(null);
        }
    }

    @Transactional
    public Coupon update(Long couponId, UpdateUnusedCouponReq req) {
        Coupon coupon = couponRepo.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        boolean used = couponRepo.isCouponUsed(couponId);

        if (used) {
            throw new BadRequestException("Used coupon can only update quantity");
        }

        if (req.getCode() != null) {
            coupon.setCode(req.getCode().trim().toUpperCase());
        }
        if (req.getCreatorType() != null) {
            coupon.setCreatorType(req.getCreatorType());
        }
        if (req.getDiscountType() != null) {
            coupon.setDiscountType(req.getDiscountType());
        }
        if (req.getDiscountValue() != null) {
            coupon.setDiscountValue(req.getDiscountValue());
        }
        if (req.getMinimumOrder() != null) {
            coupon.setMinimumOrder(req.getMinimumOrder());
        }
        if (req.getMaximumDiscount() != null) {
            coupon.setMaximumDiscount(req.getMaximumDiscount());
        }
        if (req.getQuantity() != null) {
            coupon.setQuantity(req.getQuantity());
        }
        if (req.getStartDate() != null) {
            coupon.setStartDate(req.getStartDate());
        }
        if (req.getEndDate() != null) {
            coupon.setEndDate(req.getEndDate());
        }
        validateCoupon(coupon);
        return couponRepo.save(coupon);
    }

    @Transactional
    public Coupon updateUsedCoupon(Long couponId, UpdateUsedCouponReq req) {
        Coupon coupon = couponRepo.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        if (req.getQuantity() != null) {
            coupon.setQuantity(req.getQuantity());
        }
        if (req.getEndDate() != null) {
            coupon.setEndDate(req.getEndDate());
        }
        validateCoupon(coupon);
        return couponRepo.save(coupon);
    }

    public Coupon validateAndGet(Long couponId, CreatorType expectedType, BigDecimal orderAmount) {
        Coupon coupon = findById(couponId);

        if (coupon.getCreatorType() != expectedType) {
            throw new BadRequestException("Invalid coupon type.");
        }

        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new BadRequestException("This coupon is not active.");
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(coupon.getStartDate()) || today.isAfter(coupon.getEndDate())) {
            throw new BadRequestException("This coupon has expired or is not yet valid.");
        }

        if (coupon.getMinimumOrder() != null
                && orderAmount.compareTo(coupon.getMinimumOrder()) < 0) {
            throw new BadRequestException(
                    "Order amount does not meet the minimum requirement for this coupon.");
        }

        if (coupon.getQuantity() != null && coupon.getQuantity() <= 0) {
            throw new BadRequestException("This coupon has been fully redeemed.");
        }

        return coupon;
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal amount) {
        BigDecimal discount;

        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = amount
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue();
        }

        if (coupon.getMaximumDiscount() != null
                && discount.compareTo(coupon.getMaximumDiscount()) > 0) {
            discount = coupon.getMaximumDiscount();
        }

        if (discount.compareTo(amount) > 0) {
            discount = amount;
        }

        return discount;
    }
}
