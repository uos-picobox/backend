package com.uos.picobox.domain.movie.service;

import com.uos.picobox.domain.movie.dto.movie.MovieRequestDto;
import com.uos.picobox.domain.movie.dto.movie.MovieResponseDto;
import com.uos.picobox.domain.movie.entity.*;
import com.uos.picobox.domain.movie.repository.*;
import com.uos.picobox.global.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;
    private final DistributorRepository distributorRepository;
    private final MovieRatingRepository movieRatingRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final ActorRepository actorRepository;
    private final S3Service s3Service;

    @Transactional
    public MovieResponseDto registerMovie(MovieRequestDto requestDto, MultipartFile posterImageFile) {
        LocalDate releaseDate = requestDto.getReleaseDate();

        movieRepository.findByTitleAndReleaseDate(requestDto.getTitle(), releaseDate)
                .ifPresent(m -> {
                    throw new IllegalArgumentException("이미 동일한 제목과 개봉일의 영화가 존재합니다.");
                });

        Distributor distributor = distributorRepository.findById(requestDto.getDistributorId())
                .orElseThrow(() -> new EntityNotFoundException("배급사를 찾을 수 없습니다: ID " + requestDto.getDistributorId()));
        MovieRating movieRating = movieRatingRepository.findById(requestDto.getMovieRatingId())
                .orElseThrow(() -> new EntityNotFoundException("영화 등급을 찾을 수 없습니다: ID " + requestDto.getMovieRatingId()));

        String posterS3Url = null;
        if (posterImageFile != null && !posterImageFile.isEmpty()) {
            try {
                posterS3Url = s3Service.upload(posterImageFile, "movie-posters");
            } catch (IOException | SdkException e) {
                log.error("MovieService: 포스터 이미지 업로드 실패.", e);
                throw new RuntimeException("포스터 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }

        Movie movie = Movie.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .duration(requestDto.getDuration())
                .releaseDate(releaseDate)
                .language(requestDto.getLanguage())
                .director(requestDto.getDirector())
                .distributor(distributor)
                .movieRating(movieRating)
                .posterUrl(posterS3Url)
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
    public MovieResponseDto editMovie(Long movieId, MovieRequestDto requestDto, MultipartFile posterImageFile) {
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

        String currentPosterUrl = movie.getPosterUrl();
        String finalPosterS3Url = currentPosterUrl;

        if (posterImageFile != null && !posterImageFile.isEmpty()) {
            if (StringUtils.hasText(currentPosterUrl)) {
                try {
                    s3Service.delete(currentPosterUrl);
                } catch (Exception e) {
                    log.warn("기존 포스터 이미지 S3 삭제 중 오류 발생 (무시하고 진행): " + e.getMessage());
                }
            }
            try {
                finalPosterS3Url = s3Service.upload(posterImageFile, "movie-posters");
            } catch (IOException | SdkException e) {
                log.error("MovieService: 포스터 이미지 수정 업로드 실패.", e);
                throw new RuntimeException("포스터 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }
        movie.updatePosterUrl(finalPosterS3Url);

        // 장르 매핑 업데이트
        movie.clearGenreMappings();
        if (!CollectionUtils.isEmpty(requestDto.getGenreIds())) {
            requestDto.getGenreIds().forEach(genreId -> {
                MovieGenre movieGenre = movieGenreRepository.findById(genreId)
                        .orElseThrow(() -> new EntityNotFoundException("장르를 찾을 수 없습니다: ID " + genreId));
                movie.addGenreMapping(new MovieGenreMapping(movie, movieGenre));
            });
        }

        // 출연진 매핑 업데이트
        movie.clearMovieCasts();
        if (!CollectionUtils.isEmpty(requestDto.getMovieCasts())) {
            requestDto.getMovieCasts().forEach(castDto -> {
                Actor actor = actorRepository.findById(castDto.getActorId())
                        .orElseThrow(() -> new EntityNotFoundException("배우를 찾을 수 없습니다: ID " + castDto.getActorId()));
                movie.addMovieCast(new MovieCast(movie, actor, castDto.getRole()));
            });
        }

        movieRepository.save(movie);
        return new MovieResponseDto(movie);
    }

    @Transactional
    public MovieResponseDto uploadMoviePoster(Long movieId, MultipartFile posterImageFile) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 영화를 찾을 수 없습니다: " + movieId));

        String newPosterS3Url = null;

        if (StringUtils.hasText(movie.getPosterUrl())) {
            try {
                s3Service.delete(movie.getPosterUrl());
            } catch (Exception e) {
                log.warn("포스터 변경/삭제 시 기존 S3 이미지 삭제 중 오류 발생 (무시하고 진행): " + e.getMessage());
            }
        }

        if (posterImageFile != null && !posterImageFile.isEmpty()) {
            try {
                newPosterS3Url = s3Service.upload(posterImageFile, "movie-posters");
            } catch (IOException | SdkException e) {
                log.error("MovieService: 포스터 이미지 업로드 실패 (movieId: {}).", movieId, e);
                throw new RuntimeException("포스터 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }

        movie.updatePosterUrl(newPosterS3Url);
        movieRepository.save(movie);
        return new MovieResponseDto(movie);
    }

    @Transactional
    public void removeMovie(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 영화를 찾을 수 없습니다: " + movieId));

        if (StringUtils.hasText(movie.getPosterUrl())) {
            try {
                s3Service.delete(movie.getPosterUrl());
            } catch (Exception e) {
                log.warn("영화 삭제 중 S3 포스터 이미지 삭제 실패 (무시하고 진행): " + e.getMessage());
            }
        }
        movieRepository.delete(movie);
    }
}