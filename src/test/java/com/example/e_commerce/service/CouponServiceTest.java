package com.example.e_commerce.service;

import com.example.e_commerce.constant.*;
import com.example.e_commerce.dto.request.CreateCouponReq;
import com.example.e_commerce.dto.request.UpdateUnusedCouponReq;
import com.example.e_commerce.dto.request.UpdateUsedCouponReq;
import com.example.e_commerce.entity.*;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Coupon Service tests")
class CouponServiceTest {

    @Mock private CouponRepository couponRepo;

    private CouponService couponService;

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        couponService = new CouponService(couponRepo);

        coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCode("SALE10");
        coupon.setCreatorType(CreatorType.SYSTEM);
        coupon.setDiscountType(DiscountType.PERCENTAGE);
        coupon.setDiscountValue(BigDecimal.valueOf(10));
        coupon.setMaximumDiscount(BigDecimal.valueOf(50_000));
        coupon.setStatus(CouponStatus.ACTIVE);
        coupon.setStartDate(LocalDate.now().minusDays(1));
        coupon.setEndDate(LocalDate.now().plusDays(1));
        coupon.setQuantity(10L);
    }

    private User buildUserWithRole(RoleType roleType) {
        User user = new User();
        Role role = new Role();
        role.setRoleName(roleType);
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        user.setUserRoles(Set.of(userRole));
        return user;
    }

    @Nested
    @DisplayName("Create")
    class Create {
        @DisplayName("Success")
        @Test
        void success() {
            CreateCouponReq req = new CreateCouponReq();
            req.setCode("NEW10");
            req.setStartDate(LocalDate.now());
            req.setEndDate(LocalDate.now().plusDays(10));
            req.setDiscountValue(BigDecimal.valueOf(20));
            req.setQuantity(100L);
            req.setDiscountType(DiscountType.PERCENTAGE);
            req.setMinimumOrder(BigDecimal.valueOf(100_000));
            req.setMaximumDiscount(BigDecimal.valueOf(50_000));

            when(couponRepo.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

            Coupon result = couponService.create(req, CreatorType.SYSTEM);

            assertEquals("NEW10", result.getCode());
            assertEquals(CreatorType.SYSTEM, result.getCreatorType());
            assertEquals(0, BigDecimal.valueOf(100_000).compareTo(result.getMinimumOrder()));
            assertEquals(0, BigDecimal.valueOf(50_000).compareTo(result.getMaximumDiscount()));
        }
        @DisplayName("Without minimum order does not set optional fields")
        @Test
        void withoutMinimumOrder_doesNotSetOptionalFields() {
            CreateCouponReq req = new CreateCouponReq();
            req.setCode("NEW20");
            req.setStartDate(LocalDate.now());
            req.setEndDate(LocalDate.now().plusDays(10));
            req.setDiscountValue(BigDecimal.valueOf(20));
            req.setQuantity(100L);
            req.setDiscountType(DiscountType.PERCENTAGE);
            req.setMinimumOrder(null);
            req.setMaximumDiscount(BigDecimal.valueOf(50_000));

            when(couponRepo.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

            Coupon result = couponService.create(req, CreatorType.SYSTEM);

            assertNull(result.getMinimumOrder());
            assertNull(result.getMaximumDiscount());
        }
    }

    @Nested
    @DisplayName("Find By ID")
    class FindById {
        @DisplayName("Found returns coupon")
        @Test
        void found_returnsCoupon() {
            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));

            Coupon result = couponService.findById(1L);

            assertEquals(coupon, result);
        }
        @DisplayName("Not found throws resource not found")
        @Test
        void notFound_throwsResourceNotFound() {
            when(couponRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> couponService.findById(999L));
        }
    }

    @Nested
    @DisplayName("Update Status")
    class UpdateStatus {
        @DisplayName("Admin system coupon success")
        @Test
        void admin_systemCoupon_success() {
            User admin = buildUserWithRole(RoleType.ADMIN);
            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));
            when(couponRepo.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

            Coupon result = couponService.updateStatus(admin, 1L, CouponStatus.INACTIVE);

            assertEquals(CouponStatus.INACTIVE, result.getStatus());
        }
        @DisplayName("Admin store coupon throws forbidden")
        @Test
        void admin_storeCoupon_throwsForbidden() {
            coupon.setCreatorType(CreatorType.STORE);
            User admin = buildUserWithRole(RoleType.ADMIN);
            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));

            assertThrows(ForbiddenException.class,
                    () -> couponService.updateStatus(admin, 1L, CouponStatus.INACTIVE));

            verify(couponRepo, never()).save(any());
        }
        @DisplayName("Seller store coupon throws forbidden")
        @Test
        void seller_storeCoupon_throwsForbidden() {
            coupon.setCreatorType(CreatorType.STORE);
            User seller = buildUserWithRole(RoleType.SELLER);
            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));

            assertThrows(ForbiddenException.class,
                    () -> couponService.updateStatus(seller, 1L, CouponStatus.INACTIVE));

            verify(couponRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {
        @DisplayName("Unused success")
        @Test
        void unused_success() {
            UpdateUnusedCouponReq req = new UpdateUnusedCouponReq();
            req.setCode("updated");
            req.setDiscountType(DiscountType.FIXED_AMOUNT);
            req.setDiscountValue(BigDecimal.valueOf(20_000));
            req.setStartDate(LocalDate.now());
            req.setEndDate(LocalDate.now().plusDays(5));

            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));
            when(couponRepo.isCouponUsed(1L)).thenReturn(false);
            when(couponRepo.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

            Coupon result = couponService.update(1L, req);

            assertEquals("UPDATED", result.getCode());
            assertEquals(DiscountType.FIXED_AMOUNT, result.getDiscountType());
            assertNull(result.getMaximumDiscount()); // bị clear do validateCoupon với FIXED_AMOUNT
        }
        @DisplayName("Used throws bad request")
        @Test
        void used_throwsBadRequest() {
            UpdateUnusedCouponReq req = new UpdateUnusedCouponReq();

            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));
            when(couponRepo.isCouponUsed(1L)).thenReturn(true);

            assertThrows(BadRequestException.class,
                    () -> couponService.update(1L, req));

            verify(couponRepo, never()).save(any());
        }
        @DisplayName("Invalid date range throws bad request")
        @Test
        void invalidDateRange_throwsBadRequest() {
            UpdateUnusedCouponReq req = new UpdateUnusedCouponReq();
            req.setStartDate(LocalDate.now().plusDays(10));
            req.setEndDate(LocalDate.now());

            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));
            when(couponRepo.isCouponUsed(1L)).thenReturn(false);

            assertThrows(BadRequestException.class,
                    () -> couponService.update(1L, req));

            verify(couponRepo, never()).save(any());
        }
        @DisplayName("Percentage over100 throws bad request")
        @Test
        void percentageOver100_throwsBadRequest() {
            UpdateUnusedCouponReq req = new UpdateUnusedCouponReq();
            req.setDiscountType(DiscountType.PERCENTAGE);
            req.setDiscountValue(BigDecimal.valueOf(150));

            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));
            when(couponRepo.isCouponUsed(1L)).thenReturn(false);

            assertThrows(BadRequestException.class,
                    () -> couponService.update(1L, req));

            verify(couponRepo, never()).save(any());
        }
        @DisplayName("Percentage missing max discount throws bad request")
        @Test
        void percentageMissingMaxDiscount_throwsBadRequest() {
            coupon.setMaximumDiscount(null);
            UpdateUnusedCouponReq req = new UpdateUnusedCouponReq();
            req.setDiscountType(DiscountType.PERCENTAGE);
            req.setDiscountValue(BigDecimal.valueOf(20));

            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));
            when(couponRepo.isCouponUsed(1L)).thenReturn(false);

            assertThrows(BadRequestException.class,
                    () -> couponService.update(1L, req));

            verify(couponRepo, never()).save(any());
        }
        @DisplayName("Not found throws resource not found")
        @Test
        void notFound_throwsResourceNotFound() {
            UpdateUnusedCouponReq req = new UpdateUnusedCouponReq();

            when(couponRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> couponService.update(999L, req));
        }
    }

    @Nested
    @DisplayName("Update Used Coupon")
    class UpdateUsedCoupon {
        @DisplayName("Success")
        @Test
        void success() {
            UpdateUsedCouponReq req = new UpdateUsedCouponReq();
            req.setQuantity(50L);
            req.setEndDate(LocalDate.now().plusDays(30));

            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));
            when(couponRepo.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

            Coupon result = couponService.updateUsedCoupon(1L, req);

            assertEquals(50L, result.getQuantity());
            assertEquals(req.getEndDate(), result.getEndDate());
        }
        @DisplayName("Invalid end date throws bad request")
        @Test
        void invalidEndDate_throwsBadRequest() {
            UpdateUsedCouponReq req = new UpdateUsedCouponReq();
            req.setEndDate(coupon.getStartDate().minusDays(1));

            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));

            assertThrows(BadRequestException.class,
                    () -> couponService.updateUsedCoupon(1L, req));

            verify(couponRepo, never()).save(any());
        }
        @DisplayName("Not found throws resource not found")
        @Test
        void notFound_throwsResourceNotFound() {
            UpdateUsedCouponReq req = new UpdateUsedCouponReq();

            when(couponRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> couponService.updateUsedCoupon(999L, req));
        }
    }

    @Nested
    @DisplayName("Validate And Get")
    class ValidateAndGet {
        @DisplayName("Valid returns coupon")
        @Test
        void valid_returnsCoupon() {
            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));

            Coupon result = couponService.validateAndGet(1L, CreatorType.SYSTEM, BigDecimal.valueOf(200_000));

            assertEquals(coupon, result);
        }
        @DisplayName("Wrong creator type throws bad request")
        @Test
        void wrongCreatorType_throwsBadRequest() {
            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));

            assertThrows(BadRequestException.class,
                    () -> couponService.validateAndGet(1L, CreatorType.STORE, BigDecimal.valueOf(200_000)));
        }
        @DisplayName("Not active throws bad request")
        @Test
        void notActive_throwsBadRequest() {
            coupon.setStatus(CouponStatus.INACTIVE);
            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));

            assertThrows(BadRequestException.class,
                    () -> couponService.validateAndGet(1L, CreatorType.SYSTEM, BigDecimal.valueOf(200_000)));
        }
        @DisplayName("Expired throws bad request")
        @Test
        void expired_throwsBadRequest() {
            coupon.setStartDate(LocalDate.now().minusDays(10));
            coupon.setEndDate(LocalDate.now().minusDays(1));
            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));

            assertThrows(BadRequestException.class,
                    () -> couponService.validateAndGet(1L, CreatorType.SYSTEM, BigDecimal.valueOf(200_000)));
        }
        @DisplayName("Not started yet throws bad request")
        @Test
        void notStartedYet_throwsBadRequest() {
            coupon.setStartDate(LocalDate.now().plusDays(1));
            coupon.setEndDate(LocalDate.now().plusDays(10));
            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));

            assertThrows(BadRequestException.class,
                    () -> couponService.validateAndGet(1L, CreatorType.SYSTEM, BigDecimal.valueOf(200_000)));
        }
        @DisplayName("Below minimum order throws bad request")
        @Test
        void belowMinimumOrder_throwsBadRequest() {
            coupon.setMinimumOrder(BigDecimal.valueOf(500_000));
            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));

            assertThrows(BadRequestException.class,
                    () -> couponService.validateAndGet(1L, CreatorType.SYSTEM, BigDecimal.valueOf(200_000)));
        }
        @DisplayName("Quantity zero throws bad request")
        @Test
        void quantityZero_throwsBadRequest() {
            coupon.setQuantity(0L);
            when(couponRepo.findById(1L)).thenReturn(Optional.of(coupon));

            assertThrows(BadRequestException.class,
                    () -> couponService.validateAndGet(1L, CreatorType.SYSTEM, BigDecimal.valueOf(200_000)));
        }
    }

    @Nested
    @DisplayName("Calculate Discount")
    class CalculateDiscount {
        @DisplayName("Percentage calculates correctly")
        @Test
        void percentage_calculatesCorrectly() {
            coupon.setDiscountType(DiscountType.PERCENTAGE);
            coupon.setDiscountValue(BigDecimal.valueOf(10));
            coupon.setMaximumDiscount(BigDecimal.valueOf(1_000_000));

            BigDecimal result = couponService.calculateDiscount(coupon, BigDecimal.valueOf(200_000));

            assertEquals(0, BigDecimal.valueOf(20_000).compareTo(result));
        }
        @DisplayName("Percentage exceeds maximum discount caps at max")
        @Test
        void percentage_exceedsMaximumDiscount_capsAtMax() {
            coupon.setDiscountType(DiscountType.PERCENTAGE);
            coupon.setDiscountValue(BigDecimal.valueOf(50));
            coupon.setMaximumDiscount(BigDecimal.valueOf(30_000));

            BigDecimal result = couponService.calculateDiscount(coupon, BigDecimal.valueOf(200_000));

            assertEquals(0, BigDecimal.valueOf(30_000).compareTo(result));
        }
        @DisplayName("Fixed amount returns exact value")
        @Test
        void fixedAmount_returnsExactValue() {
            coupon.setDiscountType(DiscountType.FIXED_AMOUNT);
            coupon.setDiscountValue(BigDecimal.valueOf(15_000));
            coupon.setMaximumDiscount(null);

            BigDecimal result = couponService.calculateDiscount(coupon, BigDecimal.valueOf(200_000));

            assertEquals(0, BigDecimal.valueOf(15_000).compareTo(result));
        }
        @DisplayName("Discount exceeds amount caps at amount")
        @Test
        void discountExceedsAmount_capsAtAmount() {
            coupon.setDiscountType(DiscountType.FIXED_AMOUNT);
            coupon.setDiscountValue(BigDecimal.valueOf(500_000));
            coupon.setMaximumDiscount(null);

            BigDecimal result = couponService.calculateDiscount(coupon, BigDecimal.valueOf(200_000));

            assertEquals(0, BigDecimal.valueOf(200_000).compareTo(result));
        }
    }
}