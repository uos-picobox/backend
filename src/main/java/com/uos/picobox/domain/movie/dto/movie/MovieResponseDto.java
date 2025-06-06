package com.uos.picobox.domain.movie.dto.movie;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uos.picobox.domain.movie.dto.distributor.DistributorResponseDto;
import com.uos.picobox.domain.movie.dto.genre.MovieGenreResponseDto;
import com.uos.picobox.domain.movie.dto.rating.MovieRatingResponseDto;
import com.uos.picobox.domain.movie.entity.Movie;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MovieResponseDto {

    @Schema(description = "영화 ID", example = "1")
    private Long movieId;

    @Schema(description = "영화 제목", example = "극한직업")
    private String title;

    @Schema(description = "영화 설명", example = "낮에는 치킨장사, 밤에는 잠복근무...")
    private String description;

    @Schema(description = "상영 시간 (분 단위)", example = "111")
    private Integer duration;

    @Schema(description = "개봉일 (yyyy-MM-dd)", example = "2019-01-23")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @Schema(description = "상영 종료 예정일 (yyyy-MM-dd)", example = "2025-08-31", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate screeningEndDate;

    @Schema(description = "언어", example = "한국어")
    private String language;

    @Schema(description = "감독", example = "이병헌")
    private String director;

    @Schema(description = "배급사 정보")
    private DistributorResponseDto distributor;

    @Schema(description = "영화 등급 정보")
    private MovieRatingResponseDto movieRating;

    @Schema(description = "영화 장르 목록")
    private List<MovieGenreResponseDto> genres;

    @Schema(description = "출연진 목록")
    private List<MovieCastMemberResponseDto> movieCasts;

    @Schema(description = "포스터 이미지 URL")
    private String posterUrl;

    @Getter
    public static class MovieCastMemberResponseDto {
        @Schema(description = "배우 정보")
        private ActorSummaryDto actor;

        @Schema(description = "영화 내 역할", example = "고반장")
        private String role;

        public MovieCastMemberResponseDto(Long actorId, String actorName, String role) {
            this.actor = new ActorSummaryDto(actorId, actorName);
            this.role = role;
        }
    }

    // 배우 요약 정보 DTO
    @Getter
    public static class ActorSummaryDto {
        @Schema(description = "배우 ID", example = "1")
        private Long actorId;
        @Schema(description = "배우 이름", example = "류승룡")
        private String name;

        public ActorSummaryDto(Long actorId, String name) {
            this.actorId = actorId;
            this.name = name;
        }
    }


    public MovieResponseDto(Movie movie) {
        this.movieId = movie.getId();
        this.title = movie.getTitle();
        this.description = movie.getDescription();
        this.duration = movie.getDuration();
        this.releaseDate = movie.getReleaseDate();
        this.screeningEndDate = movie.getScreeningEndDate();
        this.language = movie.getLanguage();
        this.director = movie.getDirector();
        this.distributor = new DistributorResponseDto(movie.getDistributor());
        this.movieRating = new MovieRatingResponseDto(movie.getMovieRating());
        this.posterUrl = movie.getPosterUrl();

        this.genres = movie.getGenreMappings().stream()
                .map(mapping -> new MovieGenreResponseDto(mapping.getMovieGenre()))
                .collect(Collectors.toList());

        this.movieCasts = movie.getMovieCasts().stream()
                .map(cast -> new MovieCastMemberResponseDto(
                        cast.getActor().getId(),
                        cast.getActor().getName(),
                        cast.getRole()))
                .collect(Collectors.toList());
    }
}