package com.uos.picobox.domain.movie.service;

import com.uos.picobox.domain.movie.dto.genre.MovieGenreRequestDto;
import com.uos.picobox.domain.movie.dto.genre.MovieGenreResponseDto;
import com.uos.picobox.domain.movie.entity.MovieGenre;
import com.uos.picobox.domain.movie.repository.MovieGenreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieGenreService {

    private final MovieGenreRepository movieGenreRepository;

    @Transactional
    public MovieGenreResponseDto registerGenre(MovieGenreRequestDto movieGenreRequestDto) {
        movieGenreRepository.findByGenreName(movieGenreRequestDto.getGenreName())
                .ifPresent(m -> {
                    throw new IllegalArgumentException("이미 존재하는 장르명입니다: " + movieGenreRequestDto.getGenreName());
                });

        MovieGenre movieGenre = MovieGenre.builder()
                .genreName(movieGenreRequestDto.getGenreName())
                .build();
        MovieGenre savedGenre = movieGenreRepository.save(movieGenre);
        return new MovieGenreResponseDto(savedGenre);
    }

    public List<MovieGenreResponseDto> findAllGenres() {
        return movieGenreRepository.findAll().stream()
                .map(MovieGenreResponseDto::new)
                .collect(Collectors.toList());
    }

    public MovieGenreResponseDto findGenreById(Long genreId) {
        MovieGenre movieGenre = movieGenreRepository.findById(genreId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 장르를 찾을 수 없습니다: " + genreId));
        return new MovieGenreResponseDto(movieGenre);
    }

    @Transactional
    public MovieGenreResponseDto editGenre(Long genreId, MovieGenreRequestDto movieGenreRequestDto) {
        MovieGenre movieGenre = movieGenreRepository.findById(genreId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 장르를 찾을 수 없습니다: " + genreId));

        if (!movieGenre.getGenreName().equals(movieGenreRequestDto.getGenreName())) {
            movieGenreRepository.findByGenreName(movieGenreRequestDto.getGenreName())
                    .ifPresent(m -> {
                        throw new IllegalArgumentException("이미 존재하는 장르명입니다: " + movieGenreRequestDto.getGenreName());
                    });
        }

        movieGenre.updateGenreName(movieGenreRequestDto.getGenreName());
        return new MovieGenreResponseDto(movieGenre);
    }

    @Transactional
    public void removeGenre(Long genreId) {
        if (!movieGenreRepository.existsById(genreId)) {
            throw new EntityNotFoundException("해당 ID의 장르를 찾을 수 없습니다: " + genreId);
        }
        movieGenreRepository.deleteById(genreId);
    }
}