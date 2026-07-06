package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(UUID userId, Pageable pageable);

    Optional<Order> findByOrderCode(String orderCode);

    @EntityGraph(attributePaths = {
            "orderStores", "orderStores.orderItems", "orderStores.store", "address"
    })
    Optional<Order> findWithDetailsById(Long id);
}
