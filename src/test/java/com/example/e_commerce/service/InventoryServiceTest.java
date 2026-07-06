package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.UpdateQuantityReq;
import com.example.e_commerce.dto.response.InventoryRes;
import com.example.e_commerce.entity.Inventory;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.InventoryMapper;
import com.example.e_commerce.repository.InventoryRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory Service tests")
class InventoryServiceTest {

    @Mock private InventoryRepository inventoryRepo;
    @Mock private ProductVariantService productVariantService;
    @Mock private InventoryMapper mapper;

    @InjectMocks
    private InventoryService inventoryService;

    @Nested
    @DisplayName("Create Batch")
    class CreateBatch {
        @DisplayName("Should save and return inventories")
        @Test
        void shouldSaveAndReturnInventories() {
            Inventory inv1 = new Inventory();
            inv1.setId(1L);
            Inventory inv2 = new Inventory();
            inv2.setId(2L);
            List<Inventory> inventories = List.of(inv1, inv2);

            when(inventoryRepo.saveAll(inventories)).thenReturn(inventories);

            List<Inventory> result = inventoryService.createBatch(inventories);

            assertThat(result).containsExactly(inv1, inv2);
            verify(inventoryRepo, times(1)).saveAll(inventories);
        }
        @DisplayName("Should return empty list when input empty")
        @Test
        void shouldReturnEmptyList_whenInputEmpty() {
            when(inventoryRepo.saveAll(List.of())).thenReturn(List.of());

            List<Inventory> result = inventoryService.createBatch(List.of());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By ID")
    class FindById {
        @DisplayName("Should return inventory when exists")
        @Test
        void shouldReturnInventory_whenExists() {
            Inventory inventory = new Inventory();
            inventory.setId(1L);

            when(inventoryRepo.findById(1L)).thenReturn(Optional.of(inventory));

            Inventory result = inventoryService.findById(1L);

            assertThat(result).isEqualTo(inventory);
        }
        @DisplayName("Should throw when not found")
        @Test
        void shouldThrow_whenNotFound() {
            when(inventoryRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.findById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Inventory not found");
        }
    }

    @Nested
    @DisplayName("Update Quantity")
    class UpdateQuantity {
        @DisplayName("Should update quantity and save")
        @Test
        void shouldUpdateQuantityAndSave() {
            Inventory inventory = new Inventory();
            inventory.setId(1L);
            inventory.setQuantity(5);

            UpdateQuantityReq req = mock(UpdateQuantityReq.class);
            when(req.getQuantity()).thenReturn(20);
            when(inventoryRepo.findById(1L)).thenReturn(Optional.of(inventory));

            inventoryService.updateQuantity(1L, req);

            assertThat(inventory.getQuantity()).isEqualTo(20);
            verify(inventoryRepo, times(1)).save(inventory);
        }
        @DisplayName("Should throw when inventory not found")
        @Test
        void shouldThrow_whenInventoryNotFound() {
            UpdateQuantityReq req = mock(UpdateQuantityReq.class);
            when(inventoryRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.updateQuantity(1L, req))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(inventoryRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Search")
    class Search {
        @DisplayName("Should return mapped page")
        @Test
        void shouldReturnMappedPage() {
            Inventory inventory = new Inventory();
            inventory.setId(1L);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Inventory> inventoryPage = new PageImpl<>(List.of(inventory), pageable, 1);

            InventoryRes res = mock(InventoryRes.class);

            when(inventoryRepo.search("keyword", pageable)).thenReturn(inventoryPage);
            when(mapper.toInventoryRes(inventory)).thenReturn(res);

            Page<InventoryRes> result = inventoryService.search("keyword", pageable);

            assertThat(result.getContent()).containsExactly(res);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
        @DisplayName("Should return empty page when no match")
        @Test
        void shouldReturnEmptyPage_whenNoMatch() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Inventory> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(inventoryRepo.search("nonexistent", pageable)).thenReturn(emptyPage);

            Page<InventoryRes> result = inventoryService.search("nonexistent", pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verifyNoInteractions(mapper);
        }
        @DisplayName("Should allow null keyword")
        @Test
        void shouldAllowNullKeyword() {
            Inventory inventory = new Inventory();
            inventory.setId(1L);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Inventory> inventoryPage = new PageImpl<>(List.of(inventory), pageable, 1);
            InventoryRes res = mock(InventoryRes.class);

            when(inventoryRepo.search(null, pageable)).thenReturn(inventoryPage);
            when(mapper.toInventoryRes(inventory)).thenReturn(res);

            Page<InventoryRes> result = inventoryService.search(null, pageable);

            assertThat(result.getContent()).containsExactly(res);
        }
    }
}