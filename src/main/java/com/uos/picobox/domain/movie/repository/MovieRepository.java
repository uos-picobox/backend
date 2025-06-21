package com.uos.picobox.domain.movie.repository;

import com.uos.picobox.domain.movie.entity.Movie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    @Override
    @EntityGraph(attributePaths = {"distributor", "movieRating", "genreMappings", "genreMappings.movieGenre", "movieCasts", "movieCasts.actor"})
    Optional<Movie> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"distributor", "movieRating"})
    List<Movie> findAll();

    Optional<Movie> findByTitleAndReleaseDate(String title, java.time.LocalDate releaseDate);

    /**
     * 모든 영화 목록을 모든 상세 정보와 함께 조회합니다.
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN FETCH m.distributor " +
            "LEFT JOIN FETCH m.movieRating " +
            "LEFT JOIN FETCH m.genreMappings mgm JOIN FETCH mgm.movieGenre " +
            "LEFT JOIN FETCH m.movieCasts mc JOIN FETCH mc.actor")
    List<Movie> findAllWithDetails();

    /**
     * 활성(현재 상영 중 또는 상영 종료일 미정/미래) 및 개봉 예정 영화 목록을 조회합니다.
     * 개봉일 최신순으로 정렬합니다.
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN FETCH m.movieRating mr " +
            "WHERE (m.screeningEndDate IS NULL OR m.screeningEndDate >= :today) " +
            "OR m.releaseDate > :today")
    List<Movie> findActiveAndUpcomingMovies(@Param("today") LocalDate today);

    /**
     * 상세 조회용: 특정 영화의 모든 상세 정보를 조회합니다.
     */
    @Query("SELECT DISTINCT m FROM Movie m " + // 'DISTINCT' 추가
            "LEFT JOIN FETCH m.distributor " +
            "LEFT JOIN FETCH m.movieRating " +
            "LEFT JOIN FETCH m.genreMappings mgm " +
            "LEFT JOIN FETCH mgm.movieGenre " +
            "LEFT JOIN FETCH m.movieCasts mc " +
            "LEFT JOIN FETCH mc.actor " +
            "WHERE m.id = :movieId")
    Optional<Movie> findMovieDetailsById(@Param("movieId") Long movieId);
}