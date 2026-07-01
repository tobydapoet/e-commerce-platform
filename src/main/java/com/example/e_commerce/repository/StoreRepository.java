package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, Long> {
    @Query("""
            SELECT s FROM Store s
            WHERE (:keyword IS NULL OR :keyword = ''
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Store> search(@Param("keyword") String keyword, Pageable pageable);

    List<Store> findByOwnerId(UUID ownerId);
}
