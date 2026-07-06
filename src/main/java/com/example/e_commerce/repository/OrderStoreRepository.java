package com.example.e_commerce.repository;

import com.example.e_commerce.entity.OrderStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStoreRepository extends JpaRepository<OrderStore, Long> {
    @EntityGraph(attributePaths = {"orderItems", "order", "order.user"})
    Page<OrderStore> findAllByStoreId(Long storeId, Pageable pageable);
}
