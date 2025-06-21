package com.uos.picobox.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewRequestDto {

    @NotNull(message = "예매 ID는 필수입니다.")
    @Schema(description = "예매 ID", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reservationId;

    @NotNull(message = "영화 ID는 필수입니다.")
    @Schema(description = "영화 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long movieId;

    @NotNull(message = "평점은 필수입니다.")
    @DecimalMin(value = "0.5", message = "평점은 0.5 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
    @Schema(description = "평점 (0.5 단위, 0.5~5.0)", example = "4.5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double rating;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(max = 500, message = "리뷰 내용은 500자 이하여야 합니다.")
    @Schema(description = "리뷰 내용", example = "정말 재미있는 영화였습니다!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String comment;

    public ReviewRequestDto(Long reservationId, Long movieId, Double rating, String comment) {
        this.reservationId = reservationId;
        this.movieId = movieId;
        this.rating = rating;
        this.comment = comment;
    }
} 