package com.example.e_commerce.mapper;

import com.example.e_commerce.dto.response.ReviewRes;
import com.example.e_commerce.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    private final UserMapper userMapper;

    public ReviewRes toReviewRes(Review review) {
        return new ReviewRes(
                review.getId(),
                userMapper.toUserSimpleRes(review.getUser()),
                review.getProduct().getId(),
                review.getOrderItem().getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
