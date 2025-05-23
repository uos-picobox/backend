package com.uos.picobox.domain.movie.repository;

import com.uos.picobox.domain.movie.entity.Movie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
}