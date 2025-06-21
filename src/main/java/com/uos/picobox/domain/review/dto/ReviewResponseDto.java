package com.uos.picobox.domain.review.dto;

import com.uos.picobox.domain.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReviewResponseDto {

    @Schema(description = "리뷰 ID", example = "1")
    private Long reviewId;

    @Schema(description = "예매 ID", example = "123")
    private Long reservationId;

    @Schema(description = "고객 ID", example = "1")
    private Long customerId;

    @Schema(description = "고객 로그인 ID", example = "customer123")
    private String customerLoginId;

    @Schema(description = "영화 ID", example = "1")
    private Long movieId;

    @Schema(description = "영화 제목", example = "인사이드 아웃 2")
    private String movieTitle;

    @Schema(description = "평점", example = "4.5")
    private Double rating;

    @Schema(description = "리뷰 내용", example = "정말 재미있는 영화였습니다!")
    private String comment;

    @Schema(description = "좋아요 개수", example = "15")
    private Integer likeCount;

    @Schema(description = "현재 사용자가 좋아요를 눌렀는지 여부", example = "true")
    private Boolean isLikedByCurrentUser = false;

    @Schema(description = "작성 일시", example = "2024-01-15T14:30:00")
    private LocalDateTime createdAt;

    public ReviewResponseDto(Review review) {
        this.reviewId = review.getId();
        this.reservationId = review.getReservation().getId();
        this.customerId = review.getCustomer().getId();
        this.customerLoginId = review.getCustomer().getLoginId();
        this.movieId = review.getMovie().getId();
        this.movieTitle = review.getMovie().getTitle();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.likeCount = review.getLikeCount();
        this.createdAt = review.getCreatedAt();
    }

    public ReviewResponseDto(Review review, Boolean isLikedByCurrentUser) {
        this(review);
        this.isLikedByCurrentUser = isLikedByCurrentUser;
    }
} 