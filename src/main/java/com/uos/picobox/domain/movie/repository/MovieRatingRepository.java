package com.uos.picobox.domain.movie.repository;

import com.uos.picobox.domain.movie.entity.MovieRating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MovieRatingRepository extends JpaRepository<MovieRating, Long> {
    Optional<MovieRating> findByRatingName(String ratingName);
}