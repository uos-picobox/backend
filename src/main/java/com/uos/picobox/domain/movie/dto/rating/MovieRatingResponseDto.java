package com.uos.picobox.domain.movie.dto.rating;

import com.uos.picobox.domain.movie.entity.MovieRating;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class MovieRatingResponseDto {

    @Schema(description = "영화 등급 ID", example = "1")
    private Long ratingId;

    @Schema(description = "영화 등급명", example = "전체 관람가")
    private String ratingName;

    @Schema(description = "등급 설명", example = "모든 연령의 관람객이 관람할 수 있는 등급입니다.")
    private String description;

    public MovieRatingResponseDto(MovieRating movieRating) {
        this.ratingId = movieRating.getId();
        this.ratingName = movieRating.getRatingName();
        this.description = movieRating.getDescription();
    }
}