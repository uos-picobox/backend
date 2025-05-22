package com.uos.picobox.domain.movie.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MOVIE_GENRE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovieGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GENRE_ID")
    private Long id;

    @Column(name = "GENRE_NAME", nullable = false, length = 30, unique = true)
    private String genreName;

    @Builder
    public MovieGenre(String genreName) {
        this.genreName = genreName;
    }

    public void updateGenreName(String genreName) {
        this.genreName = genreName;
    }
}