package com.uos.picobox.domain.review.entity;

import com.uos.picobox.user.entity.Customer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "REVIEW_LIKE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(ReviewLike.ReviewLikeId.class)
public class ReviewLike {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID", nullable = false)
    private Review review;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @Column(name = "LIKED_AT", nullable = false)
    private LocalDateTime likedAt;

    @Builder
    public ReviewLike(Review review, Customer customer) {
        this.review = review;
        this.customer = customer;
        this.likedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.likedAt == null) {
            this.likedAt = LocalDateTime.now();
        }
    }

    // 복합키 클래스
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ReviewLikeId implements Serializable {
        private Long review;
        private Long customer;

        public ReviewLikeId(Long reviewId, Long customerId) {
            this.review = reviewId;
            this.customer = customerId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReviewLikeId that = (ReviewLikeId) o;
            return Objects.equals(review, that.review) && Objects.equals(customer, that.customer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(review, customer);
        }
    }
} 