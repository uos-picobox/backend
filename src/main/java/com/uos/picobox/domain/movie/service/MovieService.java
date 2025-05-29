package com.uos.picobox.domain.movie.service;

import com.uos.picobox.domain.movie.dto.movie.MovieRequestDto;
import com.uos.picobox.domain.movie.dto.movie.MovieResponseDto;
import com.uos.picobox.domain.movie.entity.*;
import com.uos.picobox.domain.movie.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;
    private final DistributorRepository distributorRepository;
    private final MovieRatingRepository movieRatingRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final ActorRepository actorRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    @Transactional
    public MovieResponseDto registerMovie(MovieRequestDto requestDto) {
        LocalDate releaseDate = requestDto.getReleaseDate();

        movieRepository.findByTitleAndReleaseDate(requestDto.getTitle(), releaseDate)
                .ifPresent(m -> {
                    throw new IllegalArgumentException("이미 동일한 제목과 개봉일의 영화가 존재합니다.");
                });

        Distributor distributor = distributorRepository.findById(requestDto.getDistributorId())
                .orElseThrow(() -> new EntityNotFoundException("배급사를 찾을 수 없습니다: ID " + requestDto.getDistributorId()));
        MovieRating movieRating = movieRatingRepository.findById(requestDto.getMovieRatingId())
                .orElseThrow(() -> new EntityNotFoundException("영화 등급을 찾을 수 없습니다: ID " + requestDto.getMovieRatingId()));

        Movie movie = Movie.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .duration(requestDto.getDuration())
                .releaseDate(releaseDate)
                .language(requestDto.getLanguage())
                .director(requestDto.getDirector())
                .distributor(distributor)
                .movieRating(movieRating)
                .build();

        // 장르 매핑 처리
        if (!CollectionUtils.isEmpty(requestDto.getGenreIds())) {
            requestDto.getGenreIds().forEach(genreId -> {
                MovieGenre movieGenre = movieGenreRepository.findById(genreId)
                        .orElseThrow(() -> new EntityNotFoundException("장르를 찾을 수 없습니다: ID " + genreId));
                movie.addGenreMapping(new MovieGenreMapping(movie, movieGenre));
            });
        }

        // 출연진 매핑 처리
        if (!CollectionUtils.isEmpty(requestDto.getMovieCasts())) {
            requestDto.getMovieCasts().forEach(castDto -> {
                Actor actor = actorRepository.findById(castDto.getActorId())
                        .orElseThrow(() -> new EntityNotFoundException("배우를 찾을 수 없습니다: ID " + castDto.getActorId()));
                movie.addMovieCast(new MovieCast(movie, actor, castDto.getRole()));
            });
        }

        Movie savedMovie = movieRepository.save(movie);
        return new MovieResponseDto(savedMovie);
    }

    public List<MovieResponseDto> findAllMovies() {
        return movieRepository.findAll().stream()
                .map(MovieResponseDto::new)
                .collect(Collectors.toList());
    }

    public MovieResponseDto findMovieById(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 영화를 찾을 수 없습니다: " + movieId));
        return new MovieResponseDto(movie);
    }

    @Transactional
    public MovieResponseDto editMovie(Long movieId, MovieRequestDto requestDto) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 영화를 찾을 수 없습니다: " + movieId));

        LocalDate releaseDate = requestDto.getReleaseDate();

        // 제목, 개봉일 변경 시 중복 체크
        if (!movie.getTitle().equals(requestDto.getTitle()) || !movie.getReleaseDate().equals(releaseDate)) {
            movieRepository.findByTitleAndReleaseDate(requestDto.getTitle(), releaseDate)
                    .filter(existingMovie -> !existingMovie.getId().equals(movieId))
                    .ifPresent(m -> {
                        throw new IllegalArgumentException("이미 동일한 제목과 개봉일의 다른 영화가 존재합니다.");
                    });
        }

        Distributor distributor = distributorRepository.findById(requestDto.getDistributorId())
                .orElseThrow(() -> new EntityNotFoundException("배급사를 찾을 수 없습니다: ID " + requestDto.getDistributorId()));
        MovieRating movieRating = movieRatingRepository.findById(requestDto.getMovieRatingId())
                .orElseThrow(() -> new EntityNotFoundException("영화 등급을 찾을 수 없습니다: ID " + requestDto.getMovieRatingId()));

        movie.updateDetails(
                requestDto.getTitle(),
                requestDto.getDescription(),
                requestDto.getDuration(),
                releaseDate,
                requestDto.getLanguage(),
                requestDto.getDirector(),
                distributor,
                movieRating
        );

        // 장르 매핑 업데이트 (기존 매핑 모두 삭제 후 새로 추가)
        movie.clearGenreMappings();
        if (!CollectionUtils.isEmpty(requestDto.getGenreIds())) {
            requestDto.getGenreIds().forEach(genreId -> {
                MovieGenre movieGenre = movieGenreRepository.findById(genreId)
                        .orElseThrow(() -> new EntityNotFoundException("장르를 찾을 수 없습니다: ID " + genreId));
                movie.addGenreMapping(new MovieGenreMapping(movie, movieGenre));
            });
        }

        // 출연진 매핑 업데이트 (기존 매핑 모두 삭제 후 새로 추가)
        movie.clearMovieCasts();
        if (!CollectionUtils.isEmpty(requestDto.getMovieCasts())) {
            requestDto.getMovieCasts().forEach(castDto -> {
                Actor actor = actorRepository.findById(castDto.getActorId())
                        .orElseThrow(() -> new EntityNotFoundException("배우를 찾을 수 없습니다: ID " + castDto.getActorId()));
                movie.addMovieCast(new MovieCast(movie, actor, castDto.getRole()));
            });
        }

        Movie updatedMovie = movieRepository.save(movie);
        return new MovieResponseDto(updatedMovie);
    }

    @Transactional
    public void removeMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new EntityNotFoundException("해당 ID의 영화를 찾을 수 없습니다: " + movieId);
        }
        movieRepository.deleteById(movieId);
    }
}