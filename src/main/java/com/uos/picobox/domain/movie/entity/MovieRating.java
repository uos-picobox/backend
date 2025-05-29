package com.uos.picobox.domain.movie.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MOVIE_RATING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovieRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RATING_ID")
    private Long id;

    @Column(name = "RATING_NAME", nullable = false, length = 30, unique = true)
    private String ratingName;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Builder
    public MovieRating(String ratingName, String description) {
        this.ratingName = ratingName;
        this.description = description;
    }

    public void updateDetails(String ratingName, String description) {
        this.ratingName = ratingName;
        this.description = description;
    }
}