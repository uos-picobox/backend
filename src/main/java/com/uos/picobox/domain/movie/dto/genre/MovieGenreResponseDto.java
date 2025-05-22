package com.uos.picobox.domain.movie.dto.genre;

import com.uos.picobox.domain.movie.entity.MovieGenre;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class MovieGenreResponseDto {

    @Schema(description = "장르 ID", example = "1")
    private Long genreId;

    @Schema(description = "영화 장르명", example = "드라마")
    private String genreName;

    public MovieGenreResponseDto(MovieGenre movieGenre) {
        this.genreId = movieGenre.getId();
        this.genreName = movieGenre.getGenreName();
    }
}