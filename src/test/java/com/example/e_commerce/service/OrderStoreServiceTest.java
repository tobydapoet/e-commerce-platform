package com.example.e_commerce.service;

import com.example.e_commerce.constant.OrderStoreStatus;
import com.example.e_commerce.dto.response.OrderStoreRes;
import com.example.e_commerce.entity.OrderStore;
import com.example.e_commerce.entity.Store;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.OrderStoreRepository;
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
@DisplayName("Order Store Service tests")
class OrderStoreServiceTest {

    @Mock private OrderStoreRepository orderStoreRepo;
    @Mock private StoreService storeService;

    private OrderStoreService orderStoreService;

    private User owner;
    private User otherUser;
    private Store store;
    private OrderStore orderStore;

    @BeforeEach
    void setUp() {
        orderStoreService = new OrderStoreService(orderStoreRepo, storeService);

        owner = new User();
        owner.setId(UUID.randomUUID());

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        store = new Store();
        store.setId(100L);
        store.setOwner(owner);

        orderStore = new OrderStore();
        orderStore.setId(500L);
        orderStore.setStore(store);
        orderStore.setStatus(OrderStoreStatus.PENDING);
    }

    @Nested
    @DisplayName("Get Orders By Store")
    class GetOrdersByStore {
        @DisplayName("Success")
        @Test
        void success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<OrderStore> page = new PageImpl<>(List.of(orderStore));

            when(storeService.findById(100L)).thenReturn(store);
            when(orderStoreRepo.findAllByStoreId(100L, pageable)).thenReturn(page);

            Page<OrderStoreRes> result = orderStoreService.getOrdersByStore(owner, 100L, pageable);

            assertEquals(1, result.getTotalElements());
        }
        @DisplayName("Not owner throws forbidden")
        @Test
        void notOwner_throwsForbidden() {
            Pageable pageable = PageRequest.of(0, 10);

            when(storeService.findById(100L)).thenReturn(store);

            assertThrows(ForbiddenException.class,
                    () -> orderStoreService.getOrdersByStore(otherUser, 100L, pageable));

            verify(orderStoreRepo, never()).findAllByStoreId(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("Update Status")
    class UpdateStatus {
        @DisplayName("Success")
        @Test
        void success() {
            when(orderStoreRepo.findById(500L)).thenReturn(Optional.of(orderStore));

            orderStoreService.updateStatus(owner, 500L, OrderStoreStatus.DELIVERED);

            assertEquals(OrderStoreStatus.DELIVERED, orderStore.getStatus());
            verify(orderStoreRepo).save(orderStore);
        }
        @DisplayName("Not owner throws forbidden")
        @Test
        void notOwner_throwsForbidden() {
            when(orderStoreRepo.findById(500L)).thenReturn(Optional.of(orderStore));

            assertThrows(ForbiddenException.class,
                    () -> orderStoreService.updateStatus(otherUser, 500L, OrderStoreStatus.DELIVERED));

            verify(orderStoreRepo, never()).save(any());
        }
        @DisplayName("Not found throws resource not found")
        @Test
        void notFound_throwsResourceNotFound() {
            when(orderStoreRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> orderStoreService.updateStatus(owner, 999L, OrderStoreStatus.DELIVERED));

            verify(orderStoreRepo, never()).save(any());
        }
    }
}