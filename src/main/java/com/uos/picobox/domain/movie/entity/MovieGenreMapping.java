package com.uos.picobox.domain.movie.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;


@Entity
@Table(name = "MOVIE_GENRE_MAPPING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(MovieGenreMapping.MovieGenreMappingId.class)
public class MovieGenreMapping {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOVIE_ID", nullable = false)
    @Setter
    private Movie movie;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GENRE_ID", nullable = false)
    private MovieGenre movieGenre;

    public MovieGenreMapping(Movie movie, MovieGenre movieGenre) {
        this.movie = movie;
        this.movieGenre = movieGenre;
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MovieGenreMappingId implements Serializable {
        private Long movie;
        private Long movieGenre;

        public MovieGenreMappingId(Long movieId, Long genreId) {
            this.movie = movieId;
            this.movieGenre = genreId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MovieGenreMappingId that = (MovieGenreMappingId) o;
            return Objects.equals(movie, that.movie) && Objects.equals(movieGenre, that.movieGenre);
        }

        @Override
        public int hashCode() {
            return Objects.hash(movie, movieGenre);
        }
    }
}