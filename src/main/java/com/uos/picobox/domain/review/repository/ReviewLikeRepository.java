package com.uos.picobox.domain.review.repository;

import com.uos.picobox.domain.review.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, ReviewLike.ReviewLikeId> {

    /**
     * 특정 고객이 특정 리뷰에 좋아요를 눌렀는지 확인
     */
    boolean existsByReviewIdAndCustomerId(Long reviewId, Long customerId);

    /**
     * 특정 고객이 특정 리뷰에 누른 좋아요 삭제
     */
    void deleteByReviewIdAndCustomerId(Long reviewId, Long customerId);

    /**
     * 특정 리뷰의 좋아요 개수
     */
    long countByReviewId(Long reviewId);
} 