package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.UpdateQuantityReq;
import com.example.e_commerce.dto.response.InventoryRes;
import com.example.e_commerce.entity.Inventory;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.InventoryMapper;
import com.example.e_commerce.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepo;
    private final ProductVariantService productVariantService;
    private final InventoryMapper mapper;

    public List<Inventory> createBatch(List<Inventory> inventories) {
        return inventoryRepo.saveAll(inventories);
    }

    public Inventory findById(Long id) {
        return inventoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    }

    public void updateQuantity(Long id, UpdateQuantityReq req) {
        Inventory inventory = findById(id);
        inventory.setQuantity(req.getQuantity());
        inventoryRepo.save(inventory);
    }

    public Page<InventoryRes> search(String keyword, Pageable pageable) {
        return inventoryRepo.search(keyword, pageable)
                .map(mapper::toInventoryRes);
    }
}
