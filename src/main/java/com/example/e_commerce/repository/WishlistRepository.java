package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Page<Wishlist> findByUserId(UUID userId, Pageable pageable);
    Page<Wishlist> findAll(Pageable pageable);
}
