package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.CreateCartItemReq;
import com.example.e_commerce.dto.request.UpdateQuantityReq;
import com.example.e_commerce.entity.*;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Item Service tests")
class CartItemServiceTest {

    @Mock private CartItemRepository cartItemRepo;
    @Mock private ProductVariantService productVariantService;
    @Mock private CartStoreService cartStoreService;

    private CartItemService cartItemService;

    private User currentUser;
    private User otherUser;
    private Store store;
    private Product product;
    private ProductVariant variant;
    private CartStore cartStore;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cartItemService = new CartItemService(cartItemRepo, productVariantService, cartStoreService);

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        store = new Store();
        store.setId(100L);

        product = new Product();
        product.setId(300L);
        product.setStore(store);

        variant = new ProductVariant();
        variant.setId(400L);
        variant.setProduct(product);

        cartStore = new CartStore();
        cartStore.setId(200L);
        cartStore.setUser(currentUser);
        cartStore.setStore(store);

        cartItem = new CartItem();
        cartItem.setId(500L);
        cartItem.setCartStore(cartStore);
        cartItem.setProductVariant(variant);
        cartItem.setQuantity(2);
    }

    @Nested
    @DisplayName("Create")
    class Create {
        @DisplayName("Success")
        @Test
        void success() {
            CreateCartItemReq req = new CreateCartItemReq();
            req.setProductVariantId(400L);
            req.setQuantity(3);

            when(productVariantService.findById(400L)).thenReturn(variant);
            when(cartStoreService.findOrCreate(currentUser, 100L)).thenReturn(cartStore);
            when(cartItemRepo.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

            CartItem result = cartItemService.create(currentUser, req);

            assertEquals(cartStore, result.getCartStore());
            assertEquals(variant, result.getProductVariant());
            assertEquals(3, result.getQuantity());
            verify(cartStoreService).findOrCreate(currentUser, 100L);
        }
    }

    @Nested
    @DisplayName("Find By ID")
    class FindById {
        @DisplayName("Found returns cart item")
        @Test
        void found_returnsCartItem() {
            when(cartItemRepo.findById(500L)).thenReturn(Optional.of(cartItem));

            CartItem result = cartItemService.findById(500L);

            assertEquals(cartItem, result);
        }
        @DisplayName("Not found throws resource not found")
        @Test
        void notFound_throwsResourceNotFound() {
            when(cartItemRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> cartItemService.findById(999L));
        }
    }

    @Nested
    @DisplayName("Update Quantity")
    class UpdateQuantity {
        @DisplayName("Success")
        @Test
        void success() {
            UpdateQuantityReq req = new UpdateQuantityReq();
            req.setQuantity(5);

            when(cartItemRepo.findById(500L)).thenReturn(Optional.of(cartItem));

            cartItemService.updateQuantity(currentUser, 500L, req);

            assertEquals(5, cartItem.getQuantity());
            verify(cartItemRepo).save(cartItem);
        }
        @DisplayName("Not owner throws forbidden")
        @Test
        void notOwner_throwsForbidden() {
            cartStore.setUser(otherUser);
            UpdateQuantityReq req = new UpdateQuantityReq();
            req.setQuantity(5);

            when(cartItemRepo.findById(500L)).thenReturn(Optional.of(cartItem));

            assertThrows(ForbiddenException.class,
                    () -> cartItemService.updateQuantity(currentUser, 500L, req));

            verify(cartItemRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {
        @DisplayName("Deletes cart item and empty cart store")
        @Test
        void deletesCartItemAndEmptyCartStore() {
            when(cartItemRepo.findById(500L)).thenReturn(Optional.of(cartItem));
            when(cartItemRepo.countByCartStoreId(200L)).thenReturn(0L);

            cartItemService.delete(currentUser, 500L);

            verify(cartItemRepo).delete(cartItem);
            verify(cartStoreService).delete(cartStore);
        }
        @DisplayName("Deletes cart item but keeps cart store")
        @Test
        void deletesCartItemButKeepsCartStore() {
            when(cartItemRepo.findById(500L)).thenReturn(Optional.of(cartItem));
            when(cartItemRepo.countByCartStoreId(200L)).thenReturn(2L);

            cartItemService.delete(currentUser, 500L);

            verify(cartItemRepo).delete(cartItem);
            verify(cartStoreService, never()).delete(any());
        }
        @DisplayName("Not owner throws forbidden")
        @Test
        void notOwner_throwsForbidden() {
            cartStore.setUser(otherUser);

            when(cartItemRepo.findById(500L)).thenReturn(Optional.of(cartItem));

            assertThrows(ForbiddenException.class,
                    () -> cartItemService.delete(currentUser, 500L));

            verify(cartItemRepo, never()).delete(any());
            verify(cartStoreService, never()).delete(any());
        }
    }
}