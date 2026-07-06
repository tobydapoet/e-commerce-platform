package com.example.e_commerce.service;

import com.example.e_commerce.constant.OrderStoreStatus;
import com.example.e_commerce.dto.request.CreateReviewReq;
import com.example.e_commerce.dto.response.ReviewRes;
import com.example.e_commerce.entity.Order;
import com.example.e_commerce.entity.OrderItem;
import com.example.e_commerce.entity.OrderStore;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.ProductVariant;
import com.example.e_commerce.entity.Review;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.ReviewMapper;
import com.example.e_commerce.repository.OrderItemRepository;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Review Service tests")
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepo;
    @Mock private OrderItemRepository orderItemRepo;
    @Mock private ProductRepository productRepo;
    @Mock private ReviewMapper mapper;

    private ReviewService reviewService;

    private User currentUser;
    private User otherUser;
    private Product product;
    private ProductVariant productVariant;
    private Order order;
    private OrderStore orderStore;
    private OrderItem orderItem;
    private Review review;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepo, orderItemRepo, productRepo, mapper);

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        product = new Product();
        product.setId(10L);

        productVariant = new ProductVariant();
        productVariant.setId(20L);
        productVariant.setProduct(product);

        order = new Order();
        order.setId(30L);
        order.setUser(currentUser);

        orderStore = new OrderStore();
        orderStore.setId(40L);
        orderStore.setOrder(order);
        orderStore.setStatus(OrderStoreStatus.DELIVERED);

        orderItem = new OrderItem();
        orderItem.setId(50L);
        orderItem.setOrderStore(orderStore);
        orderItem.setProductVariant(productVariant);

        review = new Review();
        review.setId(60L);
        review.setUser(currentUser);
        review.setProduct(product);
        review.setOrderItem(orderItem);
        review.setRating(5);
        review.setComment("Great");
    }

    @Nested
    @DisplayName("Create")
    class Create {
        @DisplayName("Success creates review and updates product average rating")
        @Test
        void success_createsReviewAndUpdatesProductAverageRating() {
            CreateReviewReq req = new CreateReviewReq();
            req.setOrderItemId(orderItem.getId());
            req.setRating(5);
            req.setComment("Great");

            when(orderItemRepo.findById(orderItem.getId())).thenReturn(Optional.of(orderItem));
            when(reviewRepo.existsByOrderItemId(orderItem.getId())).thenReturn(false);
            when(reviewRepo.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
            when(reviewRepo.getAverageRatingByProductId(product.getId())).thenReturn(4.5);

            Review result = reviewService.create(currentUser, req);

            assertSame(currentUser, result.getUser());
            assertSame(product, result.getProduct());
            assertSame(orderItem, result.getOrderItem());
            assertEquals(5, result.getRating());
            assertEquals("Great", result.getComment());
            assertEquals(4.5, product.getAverageRating());

            verify(reviewRepo).save(any(Review.class));
            verify(productRepo).save(product);
        }
        @DisplayName("Order item not found throws resource not found")
        @Test
        void orderItemNotFound_throwsResourceNotFound() {
            CreateReviewReq req = new CreateReviewReq();
            req.setOrderItemId(999L);

            when(orderItemRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> reviewService.create(currentUser, req));

            verifyNoInteractions(reviewRepo, productRepo);
        }
        @DisplayName("Order does not belong to user throws forbidden")
        @Test
        void orderDoesNotBelongToUser_throwsForbidden() {
            order.setUser(otherUser);
            CreateReviewReq req = new CreateReviewReq();
            req.setOrderItemId(orderItem.getId());

            when(orderItemRepo.findById(orderItem.getId())).thenReturn(Optional.of(orderItem));

            assertThrows(ForbiddenException.class,
                    () -> reviewService.create(currentUser, req));

            verify(reviewRepo, never()).save(any());
        }
        @DisplayName("Order store not delivered throws bad request")
        @Test
        void orderStoreNotDelivered_throwsBadRequest() {
            orderStore.setStatus(OrderStoreStatus.SHIPPING);
            CreateReviewReq req = new CreateReviewReq();
            req.setOrderItemId(orderItem.getId());

            when(orderItemRepo.findById(orderItem.getId())).thenReturn(Optional.of(orderItem));

            assertThrows(BadRequestException.class,
                    () -> reviewService.create(currentUser, req));

            verify(reviewRepo, never()).save(any());
        }
        @DisplayName("Already reviewed throws bad request")
        @Test
        void alreadyReviewed_throwsBadRequest() {
            CreateReviewReq req = new CreateReviewReq();
            req.setOrderItemId(orderItem.getId());

            when(orderItemRepo.findById(orderItem.getId())).thenReturn(Optional.of(orderItem));
            when(reviewRepo.existsByOrderItemId(orderItem.getId())).thenReturn(true);

            assertThrows(BadRequestException.class,
                    () -> reviewService.create(currentUser, req));

            verify(reviewRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Find By Product")
    class FindByProduct {
        @DisplayName("Product exists returns mapped review page")
        @Test
        void productExists_returnsMappedReviewPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> reviewPage = new PageImpl<>(List.of(review), pageable, 1);
            ReviewRes reviewRes = new ReviewRes(
                    review.getId(),
                    null,
                    product.getId(),
                    orderItem.getId(),
                    review.getRating(),
                    review.getComment(),
                    review.getCreatedAt()
            );

            when(productRepo.existsById(product.getId())).thenReturn(true);
            when(reviewRepo.findAllByProductId(product.getId(), pageable)).thenReturn(reviewPage);
            when(mapper.toReviewRes(review)).thenReturn(reviewRes);

            Page<ReviewRes> result = reviewService.findByProduct(product.getId(), pageable);

            assertEquals(1, result.getTotalElements());
            assertSame(reviewRes, result.getContent().get(0));
        }
        @DisplayName("Product not found throws resource not found")
        @Test
        void productNotFound_throwsResourceNotFound() {
            Pageable pageable = PageRequest.of(0, 10);

            when(productRepo.existsById(999L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> reviewService.findByProduct(999L, pageable));

            verify(reviewRepo, never()).findAllByProductId(any(), any());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {
        @DisplayName("Success deletes review and updates product average rating")
        @Test
        void success_deletesReviewAndUpdatesProductAverageRating() {
            when(reviewRepo.findById(review.getId())).thenReturn(Optional.of(review));
            when(reviewRepo.getAverageRatingByProductId(product.getId())).thenReturn(3.0);

            reviewService.delete(currentUser, review.getId());

            verify(reviewRepo).delete(review);
            verify(reviewRepo).flush();
            assertEquals(3.0, product.getAverageRating());
            verify(productRepo).save(product);
        }
        @DisplayName("Review not found throws resource not found")
        @Test
        void reviewNotFound_throwsResourceNotFound() {
            when(reviewRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> reviewService.delete(currentUser, 999L));

            verify(reviewRepo, never()).delete(any());
            verifyNoInteractions(productRepo);
        }
        @DisplayName("Review does not belong to user throws forbidden")
        @Test
        void reviewDoesNotBelongToUser_throwsForbidden() {
            review.setUser(otherUser);

            Long reviewId = review.getId();

            when(reviewRepo.findById(reviewId)).thenReturn(Optional.of(review));

            assertThrows(
                    ForbiddenException.class,
                    () -> reviewService.delete(currentUser, reviewId)
            );

            verify(reviewRepo, never()).delete(any());
            verifyNoInteractions(productRepo);
        }
    }
}
