package com.uos.picobox.domain.movie.service;

import com.uos.picobox.domain.movie.dto.rating.MovieRatingRequestDto;
import com.uos.picobox.domain.movie.dto.rating.MovieRatingResponseDto;
import com.uos.picobox.domain.movie.entity.MovieRating;
import com.uos.picobox.domain.movie.repository.MovieRatingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieRatingService {

    private final MovieRatingRepository movieRatingRepository;

    @Transactional
    public MovieRatingResponseDto registerMovieRating(MovieRatingRequestDto requestDto) {
        movieRatingRepository.findByRatingName(requestDto.getRatingName())
                .ifPresent(mr -> {
                    throw new IllegalArgumentException("이미 존재하는 영화 등급명입니다: " + requestDto.getRatingName());
                });

        MovieRating movieRating = MovieRating.builder()
                .ratingName(requestDto.getRatingName())
                .description(requestDto.getDescription())
                .build();
        MovieRating savedMovieRating = movieRatingRepository.save(movieRating);
        return new MovieRatingResponseDto(savedMovieRating);
    }

    public List<MovieRatingResponseDto> findAllMovieRatings() {
        return movieRatingRepository.findAll().stream()
                .map(MovieRatingResponseDto::new)
                .collect(Collectors.toList());
    }

    public MovieRatingResponseDto findMovieRatingById(Long ratingId) {
        MovieRating movieRating = movieRatingRepository.findById(ratingId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 영화 등급을 찾을 수 없습니다: " + ratingId));
        return new MovieRatingResponseDto(movieRating);
    }

    @Transactional
    public MovieRatingResponseDto editMovieRating(Long ratingId, MovieRatingRequestDto requestDto) {
        MovieRating movieRating = movieRatingRepository.findById(ratingId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 영화 등급을 찾을 수 없습니다: " + ratingId));

        if (!movieRating.getRatingName().equals(requestDto.getRatingName())) {
            movieRatingRepository.findByRatingName(requestDto.getRatingName())
                    .ifPresent(mr -> {
                        throw new IllegalArgumentException("이미 존재하는 영화 등급명입니다: " + requestDto.getRatingName());
                    });
        }

        movieRating.updateDetails(
                requestDto.getRatingName(),
                requestDto.getDescription()
        );
        return new MovieRatingResponseDto(movieRating);
    }

    @Transactional
    public void removeMovieRating(Long ratingId) {
        if (!movieRatingRepository.existsById(ratingId)) {
            throw new EntityNotFoundException("해당 ID의 영화 등급을 찾을 수 없습니다: " + ratingId);
        }
        movieRatingRepository.deleteById(ratingId);
    }
}