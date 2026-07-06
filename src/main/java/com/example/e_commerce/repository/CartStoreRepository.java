package com.example.e_commerce.repository;

import com.example.e_commerce.entity.CartStore;
import com.example.e_commerce.entity.Store;
import com.example.e_commerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartStoreRepository extends JpaRepository<CartStore, Long> {
    Optional<CartStore> findByUserAndStore(User user, Store store);
    Page<CartStore> findAllByUserId(UUID userId, Pageable pageable);
}
