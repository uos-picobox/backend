package com.uos.picobox.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewSummaryDto {

    @Schema(description = "영화 ID", example = "1")
    private Long movieId;

    @Schema(description = "평균 평점", example = "4.2")
    private Double averageRating;

    @Schema(description = "총 리뷰 개수", example = "42")
    private Long totalReviews;

    public ReviewSummaryDto(Long movieId, Double averageRating, Long totalReviews) {
        this.movieId = movieId;
        this.averageRating = averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0;
        this.totalReviews = totalReviews;
    }
} 