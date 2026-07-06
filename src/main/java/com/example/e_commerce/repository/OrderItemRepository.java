package com.example.e_commerce.repository;

import com.example.e_commerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findAllByOrderStoreId(Long orderStoreId);
}
