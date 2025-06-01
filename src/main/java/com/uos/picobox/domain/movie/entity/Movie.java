package com.uos.picobox.domain.movie.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "MOVIE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MOVIE_ID")
    private Long id;

    @Column(name = "TITLE", nullable = false, length = 100)
    private String title;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "DURATION", nullable = false)
    private Integer duration; // 분 단위

    @Column(name = "RELEASE_DATE", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "LANGUAGE", length = 30)
    private String language;

    @Column(name = "DIRECTOR", length = 50)
    private String director;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DISTRIBUTOR_ID", nullable = false)
    private Distributor distributor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RATING_ID", nullable = false)
    private MovieRating movieRating;

    @Column(name = "POSTER_URL", length = 500)
    private String posterUrl;

    //cascade = CascadeType.ALL, orphanRemoval = true: Movie가 저장/삭제될 때 매핑 정보도 함께 처리
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MovieGenreMapping> genreMappings = new HashSet<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MovieCast> movieCasts = new ArrayList<>();

    @Builder
    public Movie(String title, String description, Integer duration, LocalDate releaseDate,
                 String language, String director, Distributor distributor, MovieRating movieRating, String posterUrl) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.language = language;
        this.director = director;
        this.distributor = distributor;
        this.movieRating = movieRating;
        this.posterUrl = posterUrl;
    }

    public void updateDetails(String title, String description, Integer duration, LocalDate releaseDate,
                              String language, String director, Distributor distributor, MovieRating movieRating) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.language = language;
        this.director = director;
        this.distributor = distributor;
        this.movieRating = movieRating;
    }

    public void updatePosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    // 연관관계 편의 메소드 (장르)
    public void clearGenreMappings() {
        this.genreMappings.clear();
    }

    public void addGenreMapping(MovieGenreMapping genreMapping) {
        this.genreMappings.add(genreMapping);
        genreMapping.setMovie(this); // 양방향 연관관계 설정
    }

    // 연관관계 편의 메소드 (출연진)
    public void clearMovieCasts() {
        this.movieCasts.clear();
    }

    public void addMovieCast(MovieCast movieCast) {
        this.movieCasts.add(movieCast);
        movieCast.setMovie(this); // 양방향 연관관계 설정
    }
}