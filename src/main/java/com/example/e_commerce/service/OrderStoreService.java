package com.example.e_commerce.service;

import com.example.e_commerce.constant.OrderStoreStatus;
import com.example.e_commerce.dto.response.OrderStoreRes;
import com.example.e_commerce.entity.OrderStore;
import com.example.e_commerce.entity.Store;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.OrderMapper;
import com.example.e_commerce.repository.OrderStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderStoreService {
    private final OrderStoreRepository orderStoreRepo;
    private final StoreService storeService;

    public Page<OrderStoreRes> getOrdersByStore(User currentUser, Long storeId, Pageable pageable) {
        Store store = storeService.findById(storeId);
        if (!store.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not allowed to view orders of this store.");
        }
        return orderStoreRepo.findAllByStoreId(storeId, pageable)
                .map(OrderMapper::toOrderStoreRes);
    }

    @Transactional
    public void updateStatus(User currentUser, Long orderStoreId, OrderStoreStatus newStatus) {
        OrderStore orderStore = orderStoreRepo.findById(orderStoreId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));

        if (!orderStore.getStore().getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not allowed to update this order.");
        }

        orderStore.setStatus(newStatus);
        orderStoreRepo.save(orderStore);
    }
}