package com.uos.picobox.domain.movie.repository;

import com.uos.picobox.domain.movie.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    Optional<MovieGenre> findByGenreName(String genreName);

    @Query(value = "SELECT m.TITLE FROM MOVIE m " +
            "JOIN MOVIE_GENRE_MAPPING mgm ON m.MOVIE_ID = mgm.MOVIE_ID " +
            "WHERE mgm.GENRE_ID = :genreId", nativeQuery = true)
    List<String> findMovieTitlesByGenreId(@Param("genreId") Long genreId);
}