package com.example.e_commerce.repository;

import com.example.e_commerce.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByOrderItemId(Long orderItemId);
    Page<Review> findAllByProductId(Long productId, Pageable pageable);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.product.id = :productId")
    Double getAverageRatingByProductId(Long productId);
}
