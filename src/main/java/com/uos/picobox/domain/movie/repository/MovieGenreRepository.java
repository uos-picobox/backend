package com.uos.picobox.domain.movie.repository;

import com.uos.picobox.domain.movie.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    Optional<MovieGenre> findByGenreName(String genreName);
}