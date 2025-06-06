package com.uos.picobox.domain.movie.service;

import com.uos.picobox.domain.movie.dto.movie.MovieListItemDto;
import com.uos.picobox.domain.movie.dto.movie.MovieRequestDto;
import com.uos.picobox.domain.movie.dto.movie.MovieResponseDto;
import com.uos.picobox.domain.movie.entity.*;
import com.uos.picobox.domain.movie.repository.*;
import com.uos.picobox.global.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    // --- 관리자용 CRUD 메소드 ---

    @Transactional
    public MovieResponseDto registerMovie(MovieRequestDto requestDto, MultipartFile posterImageFile) {
        LocalDate releaseDate = requestDto.getReleaseDate();
        LocalDate screeningEndDate = requestDto.getScreeningEndDate();

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
                throw new RuntimeException("포스터 이미지 업로드 중 오류가 발생했습니다.", e);
            }
        }

        Movie movie = Movie.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .duration(requestDto.getDuration())
                .releaseDate(releaseDate)
                .screeningEndDate(screeningEndDate)
                .language(requestDto.getLanguage())
                .director(requestDto.getDirector())
                .distributor(distributor)
                .movieRating(movieRating)
                .posterUrl(posterS3Url)
                .build();

        if (requestDto.getGenreIds() != null) {
            requestDto.getGenreIds().forEach(genreId -> {
                MovieGenre movieGenre = movieGenreRepository.findById(genreId)
                        .orElseThrow(() -> new EntityNotFoundException("장르를 찾을 수 없습니다: ID " + genreId));
                movie.addGenreMapping(new MovieGenreMapping(movie, movieGenre));
            });
        }
        if (requestDto.getMovieCasts() != null) {
            requestDto.getMovieCasts().forEach(castDto -> {
                Actor actor = actorRepository.findById(castDto.getActorId())
                        .orElseThrow(() -> new EntityNotFoundException("배우를 찾을 수 없습니다: ID " + castDto.getActorId()));
                movie.addMovieCast(new MovieCast(movie, actor, castDto.getRole()));
            });
        }
        Movie savedMovie = movieRepository.save(movie);
        return new MovieResponseDto(savedMovie);
    }

    @Transactional
    public MovieResponseDto editMovie(Long movieId, MovieRequestDto requestDto, MultipartFile posterImageFile) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 영화를 찾을 수 없습니다: " + movieId));

        LocalDate releaseDate = requestDto.getReleaseDate();
        LocalDate screeningEndDate = requestDto.getScreeningEndDate();

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
                requestDto.getTitle(), requestDto.getDescription(), requestDto.getDuration(),
                releaseDate, screeningEndDate, requestDto.getLanguage(), requestDto.getDirector(),
                distributor, movieRating
        );

        if (posterImageFile != null && !posterImageFile.isEmpty()) {
            if (StringUtils.hasText(movie.getPosterUrl())) {
                s3Service.delete(movie.getPosterUrl());
            }
            try {
                String newPosterS3Url = s3Service.upload(posterImageFile, "movie-posters");
                movie.updatePosterUrl(newPosterS3Url);
            } catch (IOException | SdkException e) {
                log.error("MovieService: 포스터 수정 업로드 실패.", e);
                throw new RuntimeException("포스터 업로드 오류: " + e.getMessage(), e);
            }
        }

        if (requestDto.getGenreIds() != null) {
            movie.clearGenreMappings();
            if (!requestDto.getGenreIds().isEmpty()) {
                requestDto.getGenreIds().forEach(genreId -> {
                    MovieGenre movieGenre = movieGenreRepository.findById(genreId)
                            .orElseThrow(() -> new EntityNotFoundException("장르를 찾을 수 없습니다: ID " + genreId));
                    movie.addGenreMapping(new MovieGenreMapping(movie, movieGenre));
                });
            }
        }

        if (requestDto.getMovieCasts() != null) {
            movie.clearMovieCasts();
            if (!requestDto.getMovieCasts().isEmpty()) {
                requestDto.getMovieCasts().forEach(castDto -> {
                    Actor actor = actorRepository.findById(castDto.getActorId())
                            .orElseThrow(() -> new EntityNotFoundException("배우를 찾을 수 없습니다: ID " + castDto.getActorId()));
                    movie.addMovieCast(new MovieCast(movie, actor, castDto.getRole()));
                });
            }
        }
        return new MovieResponseDto(movie);
    }

    @Transactional
    public MovieResponseDto uploadMoviePoster(Long movieId, MultipartFile posterImageFile) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 영화를 찾을 수 없습니다: " + movieId));
        String newPosterS3Url = null;
        if (StringUtils.hasText(movie.getPosterUrl())) {
            s3Service.delete(movie.getPosterUrl());
        }
        if (posterImageFile != null && !posterImageFile.isEmpty()) {
            try {
                newPosterS3Url = s3Service.upload(posterImageFile, "movie-posters");
            } catch (IOException | SdkException e) {
                log.error("MovieService: 포스터 업로드 실패 (movieId: {}).", movieId, e);
                throw new RuntimeException("포스터 업로드 오류: " + e.getMessage(), e);
            }
        }
        movie.updatePosterUrl(newPosterS3Url);
        return new MovieResponseDto(movie);
    }

    @Transactional
    public void removeMovie(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 영화를 찾을 수 없습니다: " + movieId));
        if (StringUtils.hasText(movie.getPosterUrl())) {
            s3Service.delete(movie.getPosterUrl());
        }
        movieRepository.delete(movie);
    }

    public List<MovieResponseDto> findAllMoviesForAdmin() {
        return movieRepository.findAllWithDetails().stream()
                .map(MovieResponseDto::new)
                .collect(Collectors.toList());
    }

    public MovieResponseDto getMovieDetail(Long movieId) {
        Movie movie = movieRepository.findMovieDetailsById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 영화를 찾을 수 없습니다: " + movieId));
        return new MovieResponseDto(movie);
    }

    // --- 사용자용 API 메소드 ---
    public List<MovieListItemDto> getMovieListForUser() {
        LocalDate today = LocalDate.now();
        List<Movie> candidateMovies = movieRepository.findActiveAndUpcomingMovies(today);

        if (candidateMovies.isEmpty()) {
            return Collections.emptyList();
        }

        List<MovieListItemDtoInternal> processedMovies = new ArrayList<>();

        for (Movie movie : candidateMovies) {
            String status;
            LocalDate releaseDate = movie.getReleaseDate();
            LocalDate screeningEndDate = movie.getScreeningEndDate();

            if (releaseDate.isAfter(today)) {
                status = "UPCOMING";
            } else { // 개봉일 <= 오늘
                if (screeningEndDate == null || !screeningEndDate.isBefore(today)) {
                    // 상영 종료일이 없거나(미정), 오늘 이후이면 "현재 상영중"
                    status = "NOW_PLAYING";
                } else {
                    // 상영 종료일이 오늘 이전이면 "상영 종료"
                    status = "ENDED";
                }
            }

            if ("NOW_PLAYING".equals(status) || "UPCOMING".equals(status)) {
                processedMovies.add(new MovieListItemDtoInternal(movie, status));
            }
        }

        processedMovies.sort(Comparator
                .comparing(MovieListItemDtoInternal::getStatusSortOrder)
                .thenComparing(m -> m.getMovie().getReleaseDate(), Comparator.reverseOrder())
                .thenComparing(m -> m.getMovie().getId(), Comparator.reverseOrder())
        );

        List<MovieListItemDto> result = new ArrayList<>();
        for (int i = 0; i < processedMovies.size(); i++) {
            Movie movie = processedMovies.get(i).getMovie();
            result.add(MovieListItemDto.fromEntity(movie, i + 1));
        }
        return result;
    }

    @Getter
    private static class MovieListItemDtoInternal {
        private final Movie movie;
        private final String status;
        private final int statusSortOrder;
        public MovieListItemDtoInternal(Movie movie, String status) {
            this.movie = movie;
            this.status = status;
            if ("NOW_PLAYING".equals(status)) {this.statusSortOrder = 0;}
            else if ("UPCOMING".equals(status)) {this.statusSortOrder = 1;}
            else {this.statusSortOrder = 2;}
        }
    }
}