package com.uos.picobox.domain.movie.dto.movie;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uos.picobox.domain.movie.entity.Movie;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MovieListItemDto {

    @Schema(description = "예매율 순위 (상영 예정 영화는 null)", example = "1")
    private Integer rank;

    @Schema(description = "영화 ID", example = "1")
    private Long movieId;

    @Schema(description = "영화 제목", example = "서울의 봄")
    private String title;

    @Schema(description = "포스터 이미지 URL")
    private String posterUrl;

    @Schema(description = "개봉일 (yyyy-MM-dd)", example = "2023-11-22")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @Schema(description = "관람 등급명", example = "12세 이상 관람가")
    private String movieRatingName;

    @Schema(description = "상영 시간 (분 단위)", example = "141")
    private Integer duration;

    @Schema(description = "예매율 (백분율)", example = "25.50")
    private Double reservationRate;

    @Schema(description = "리뷰 평점 (소수점 둘째자리, 리뷰 없으면 null)", example = "4.25")
    private Double reviewRating;

    @Builder
    public MovieListItemDto(Integer rank, Long movieId, String title, String posterUrl,
                            LocalDate releaseDate, String movieRatingName, Integer duration,
                            Double reservationRate, Double reviewRating) {
        this.rank = rank;
        this.movieId = movieId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
        this.movieRatingName = movieRatingName;
        this.duration = duration;
        this.reservationRate = reservationRate;
        this.reviewRating = reviewRating;
    }

    public static MovieListItemDto fromEntity(Movie movie, Integer rank, Double reservationRate, Double reviewRating) {
        return MovieListItemDto.builder()
                .rank(rank)
                .movieId(movie.getId())
                .title(movie.getTitle())
                .posterUrl(movie.getPosterUrl())
                .releaseDate(movie.getReleaseDate())
                .movieRatingName(movie.getMovieRating() != null ? movie.getMovieRating().getRatingName() : null)
                .duration(movie.getDuration())
                .reservationRate(reservationRate)
                .reviewRating(reviewRating)
                .build();
    }
}