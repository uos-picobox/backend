package com.uos.picobox.domain.movie.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "MOVIE_CAST")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(MovieCast.MovieCastId.class)
public class MovieCast {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOVIE_ID", nullable = false)
    @Setter
    private Movie movie;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTOR_ID", nullable = false)
    private Actor actor;

    @Column(name = "ROLE", nullable = false, length = 50)
    private String role;

    public MovieCast(Movie movie, Actor actor, String role) {
        this.movie = movie;
        this.actor = actor;
        this.role = role;
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MovieCastId implements Serializable {
        private Long movie;
        private Long actor;

        public MovieCastId(Long movieId, Long actorId) {
            this.movie = movieId;
            this.actor = actorId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MovieCastId that = (MovieCastId) o;
            return Objects.equals(movie, that.movie) && Objects.equals(actor, that.actor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(movie, actor);
        }
    }
}