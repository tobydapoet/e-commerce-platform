package com.example.e_commerce.service;

import com.example.e_commerce.constant.OrderStoreStatus;
import com.example.e_commerce.dto.request.CreateReviewReq;
import com.example.e_commerce.dto.response.ReviewRes;
import com.example.e_commerce.entity.Order;
import com.example.e_commerce.entity.OrderItem;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.Review;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.ReviewMapper;
import com.example.e_commerce.repository.OrderItemRepository;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepo;
    private final OrderItemRepository orderItemRepo;
    private final ProductRepository productRepo;
    private final ReviewMapper mapper;

    @Transactional
    public Review create(User currentUser, CreateReviewReq req) {
        OrderItem orderItem = orderItemRepo.findById(req.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found."));

        Order order = orderItem.getOrderStore().getOrder();
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not allowed to review this item.");
        }

        OrderStoreStatus status = orderItem.getOrderStore().getStatus();
        if (status != OrderStoreStatus.DELIVERED) {
            throw new BadRequestException("You can only review items that have been delivered.");
        }

        if (reviewRepo.existsByOrderItemId(orderItem.getId())) {
            throw new BadRequestException("You have already reviewed this item.");
        }

        Product product = orderItem.getProductVariant().getProduct();

        Review review = new Review();
        review.setUser(currentUser);
        review.setProduct(product);
        review.setOrderItem(orderItem);
        review.setRating(req.getRating());
        review.setComment(req.getComment());

        Review savedReview = reviewRepo.save(review);
        updateProductAverageRating(product);
        return savedReview;
    }

    @Transactional(readOnly = true)
    public Page<ReviewRes> findByProduct(Long productId, Pageable pageable) {
        if (!productRepo.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found.");
        }
        return reviewRepo.findAllByProductId(productId, pageable)
                .map(mapper::toReviewRes);
    }

    @Transactional
    public void delete(User currentUser, Long id) {
        Review review = reviewRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found."));

        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not allowed to delete this review.");
        }

        Product product = review.getProduct();
        reviewRepo.delete(review);
        reviewRepo.flush();
        updateProductAverageRating(product);
    }

    private void updateProductAverageRating(Product product) {
        Double averageRating = reviewRepo.getAverageRatingByProductId(product.getId());
        product.setAverageRating(averageRating);
        productRepo.save(product);
    }
}
