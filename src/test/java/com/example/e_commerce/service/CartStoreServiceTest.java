package com.example.e_commerce.service;

import com.example.e_commerce.constant.CreatorType;
import com.example.e_commerce.dto.response.CartStoreRes;
import com.example.e_commerce.entity.CartStore;
import com.example.e_commerce.entity.Coupon;
import com.example.e_commerce.entity.Store;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.CartStoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Store Service tests")
class CartStoreServiceTest {

    @Mock private CartStoreRepository cartStoreRepo;
    @Mock private StoreService storeService;
    @Mock private CouponService couponService;

    private CartStoreService cartStoreService;

    private User currentUser;
    private Store store;
    private CartStore cartStore;
    private Coupon storeCoupon;

    @BeforeEach
    void setUp() {
        cartStoreService = new CartStoreService(cartStoreRepo, storeService, couponService);

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());

        store = new Store();
        store.setId(100L);
        store.setName("Test Store");

        cartStore = new CartStore();
        cartStore.setId(200L);
        cartStore.setUser(currentUser);
        cartStore.setStore(store);

        storeCoupon = new Coupon();
        storeCoupon.setId(900L);
        storeCoupon.setCreatorType(CreatorType.STORE);
        storeCoupon.setStore(store);
    }

    @Nested
    @DisplayName("Create")
    class Create {
        @DisplayName("Success")
        @Test
        void success() {
            when(storeService.findById(100L)).thenReturn(store);
            when(cartStoreRepo.save(any(CartStore.class))).thenAnswer(inv -> inv.getArgument(0));

            CartStore result = cartStoreService.create(currentUser, 100L);

            assertEquals(currentUser, result.getUser());
            assertEquals(store, result.getStore());
            verify(cartStoreRepo).save(any(CartStore.class));
        }
    }

    @Nested
    @DisplayName("Find By ID")
    class FindById {
        @DisplayName("Found returns cart store")
        @Test
        void found_returnsCartStore() {
            when(cartStoreRepo.findById(200L)).thenReturn(Optional.of(cartStore));

            CartStore result = cartStoreService.findById(200L);

            assertEquals(cartStore, result);
        }
        @DisplayName("Not found throws resource not found")
        @Test
        void notFound_throwsResourceNotFound() {
            when(cartStoreRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> cartStoreService.findById(999L));
        }
    }

    @Nested
    @DisplayName("Find Or Create")
    class FindOrCreate {
        @DisplayName("Existing returns existing")
        @Test
        void existing_returnsExisting() {
            when(storeService.findById(100L)).thenReturn(store);
            when(cartStoreRepo.findByUserAndStore(currentUser, store))
                    .thenReturn(Optional.of(cartStore));

            CartStore result = cartStoreService.findOrCreate(currentUser, 100L);

            assertEquals(cartStore, result);
            verify(cartStoreRepo, never()).save(any());
        }
        @DisplayName("Not existing creates new")
        @Test
        void notExisting_createsNew() {
            when(storeService.findById(100L)).thenReturn(store);
            when(cartStoreRepo.findByUserAndStore(currentUser, store))
                    .thenReturn(Optional.empty());
            when(cartStoreRepo.save(any(CartStore.class))).thenAnswer(inv -> inv.getArgument(0));

            CartStore result = cartStoreService.findOrCreate(currentUser, 100L);

            assertEquals(currentUser, result.getUser());
            assertEquals(store, result.getStore());
            verify(cartStoreRepo).save(any(CartStore.class));
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {
        @DisplayName("Deletes given cart store")
        @Test
        void deletesGivenCartStore() {
            cartStoreService.delete(cartStore);

            verify(cartStoreRepo).delete(cartStore);
        }
    }

    @Nested
    @DisplayName("Delete Many")
    class DeleteMany {
        @DisplayName("Deletes given list")
        @Test
        void deletesGivenList() {
            List<CartStore> list = List.of(cartStore);

            cartStoreService.deleteMany(list);

            verify(cartStoreRepo).deleteAll(list);
        }
    }

    @Nested
    @DisplayName("Add Coupon")
    class AddCoupon {
        @DisplayName("Success")
        @Test
        void success() {
            when(cartStoreRepo.findById(200L)).thenReturn(Optional.of(cartStore));
            when(couponService.findById(900L)).thenReturn(storeCoupon);
            when(cartStoreRepo.save(any(CartStore.class))).thenAnswer(inv -> inv.getArgument(0));

            CartStore result = cartStoreService.addCoupon(200L, 900L);

            assertEquals(storeCoupon, result.getStoreCoupon());
        }
        @DisplayName("Wrong creator type throws bad request")
        @Test
        void wrongCreatorType_throwsBadRequest() {
            storeCoupon.setCreatorType(CreatorType.SYSTEM);

            when(cartStoreRepo.findById(200L)).thenReturn(Optional.of(cartStore));
            when(couponService.findById(900L)).thenReturn(storeCoupon);

            assertThrows(BadRequestException.class,
                    () -> cartStoreService.addCoupon(200L, 900L));

            verify(cartStoreRepo, never()).save(any());
        }
        @DisplayName("Wrong store throws bad request")
        @Test
        void wrongStore_throwsBadRequest() {
            Store anotherStore = new Store();
            anotherStore.setId(999L);
            storeCoupon.setStore(anotherStore);

            when(cartStoreRepo.findById(200L)).thenReturn(Optional.of(cartStore));
            when(couponService.findById(900L)).thenReturn(storeCoupon);

            assertThrows(BadRequestException.class,
                    () -> cartStoreService.addCoupon(200L, 900L));

            verify(cartStoreRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Remove Coupon")
    class RemoveCoupon {
        @DisplayName("Success")
        @Test
        void success() {
            cartStore.setStoreCoupon(storeCoupon);
            when(cartStoreRepo.findById(200L)).thenReturn(Optional.of(cartStore));
            when(cartStoreRepo.save(any(CartStore.class))).thenAnswer(inv -> inv.getArgument(0));

            CartStore result = cartStoreService.removeCoupon(200L);

            assertNull(result.getStoreCoupon());
        }
        @DisplayName("No coupon throws bad request")
        @Test
        void noCoupon_throwsBadRequest() {
            cartStore.setStoreCoupon(null);
            when(cartStoreRepo.findById(200L)).thenReturn(Optional.of(cartStore));

            assertThrows(BadRequestException.class,
                    () -> cartStoreService.removeCoupon(200L));

            verify(cartStoreRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Find All By User")
    class FindAllByUser {
        @DisplayName("Returns mapped page")
        @Test
        void returnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CartStore> page = new PageImpl<>(List.of(cartStore));

            when(cartStoreRepo.findAllByUserId(currentUser.getId(), pageable)).thenReturn(page);

            Page<CartStoreRes> result = cartStoreService.findAllByUser(currentUser, pageable);

            assertEquals(1, result.getTotalElements());
        }
    }
}