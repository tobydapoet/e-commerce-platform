package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address,Long> {
    @EntityGraph(attributePaths = "user")
    @Query("""
        SELECT a
        FROM Address a
        WHERE a.user.id = :userId
        AND a.deletedAt IS NULL
        ORDER BY a.createdAt DESC
    """)
    Page<Address> findByUserId(UUID userId, Pageable pageable);

    @Query("""
        SELECT a
        FROM Address a
        WHERE a.user.id = :userId
          AND a.deletedAt IS NULL
    """)
    List<Address> findByUserIdWithoutPaging(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Address a
        SET a.isDefault = false
        WHERE a.user.id = :userId
          AND a.deletedAt IS NULL
    """)
    void clearDefaultAddress(UUID userId);
}
