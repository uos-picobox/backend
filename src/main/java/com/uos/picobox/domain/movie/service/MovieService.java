package com.uos.picobox.domain.movie.service;

import com.uos.picobox.domain.movie.dto.movie.MovieListItemDto;
import com.uos.picobox.domain.movie.dto.movie.MovieRequestDto;
import com.uos.picobox.domain.movie.dto.movie.MovieResponseDto;
import com.uos.picobox.domain.movie.entity.*;
import com.uos.picobox.domain.movie.repository.*;
import com.uos.picobox.domain.reservation.repository.ReservationRepository;
import com.uos.picobox.domain.review.repository.ReviewRepository;
import com.uos.picobox.global.service.S3Service;
import jakarta.persistence.EntityManager;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final EntityManager entityManager;
    private final MovieRepository movieRepository;
    private final DistributorRepository distributorRepository;
    private final MovieRatingRepository movieRatingRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final ActorRepository actorRepository;
    private final S3Service s3Service;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;

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
                try {
                    s3Service.delete(movie.getPosterUrl());
                } catch (SdkException e) {
                    log.error("MovieService: 포스터 삭제 실패. URL: " + movie.getPosterUrl(), e);
                }
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
            // 1. 기존 매핑을 컬렉션에서 제거 -> orphanRemoval=true로 인해 삭제 대상으로 표시됨
            movie.clearGenreMappings();
            // 2. DB에 DELETE 쿼리를 즉시 실행하고 세션을 정리
            entityManager.flush();

            // 3. 이제 세션이 깨끗해진 상태에서 새로운 매핑 추가
            if (!requestDto.getGenreIds().isEmpty()) {
                requestDto.getGenreIds().forEach(genreId -> {
                    MovieGenre movieGenre = movieGenreRepository.findById(genreId)
                            .orElseThrow(() -> new EntityNotFoundException("장르를 찾을 수 없습니다: ID " + genreId));
                    movie.addGenreMapping(new MovieGenreMapping(movie, movieGenre));
                });
            }
        }

        // --- 출연진 매핑 업데이트 로직 변경 ---
        if (requestDto.getMovieCasts() != null) {
            // 1. 기존 매핑을 컬렉션에서 제거
            movie.clearMovieCasts();
            // 2. DB에 DELETE 쿼리를 즉시 실행하고 세션을 정리
            entityManager.flush();

            // 3. 이제 새로운 매핑 추가
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
            try {
                s3Service.delete(movie.getPosterUrl());
            } catch (SdkException e) {
                log.error("MovieService: 포스터 삭제 실패. URL: " + movie.getPosterUrl(), e);
            }
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
            try {
                s3Service.delete(movie.getPosterUrl());
            } catch (SdkException e) {
                log.error("MovieService: 포스터 삭제 실패. URL: " + movie.getPosterUrl(), e);
            }
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

        // 전체 예매 관객 수 조회
        Long totalAudience = reservationRepository.countTotalReservedAudience();
        if (totalAudience == null) {
            totalAudience = 0L;
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
                // 예매율 계산
                Double reservationRate = null;
                if ("NOW_PLAYING".equals(status)) {
                    Long movieAudience = reservationRepository.countReservedAudienceByMovieId(movie.getId());
                    if (movieAudience == null) {
                        movieAudience = 0L;
                    }
                    
                    if (totalAudience > 0) {
                        double rate = (movieAudience.doubleValue() / totalAudience.doubleValue()) * 100;
                        reservationRate = BigDecimal.valueOf(rate)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue();
                    } else {
                        reservationRate = 0.0;
                    }
                }

                // 리뷰 평점 계산
                Double reviewRating = reviewRepository.calculateAverageRatingByMovieId(movie.getId());
                if (reviewRating != null) {
                    reviewRating = BigDecimal.valueOf(reviewRating)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                }

                processedMovies.add(new MovieListItemDtoInternal(movie, status, reservationRate, reviewRating));
            }
        }

        // 정렬: 현재 상영작 우선, 그 다음 예매율 높은 순, 예매율 같으면 개봉일 최신순
        processedMovies.sort(Comparator
                .comparing(MovieListItemDtoInternal::getStatusSortOrder)
                .thenComparing(m -> m.getReservationRate() != null ? m.getReservationRate() : -1.0, Comparator.reverseOrder())
                .thenComparing(m -> m.getMovie().getReleaseDate(), Comparator.reverseOrder())
                .thenComparing(m -> m.getMovie().getId(), Comparator.reverseOrder())
        );

        // 랭킹 부여 (상영 중인 영화만)
        List<MovieListItemDto> result = new ArrayList<>();
        int currentRank = 1;
        
        for (MovieListItemDtoInternal movieInternal : processedMovies) {
            Integer rank = null;
            
            // 현재 상영 중인 영화만 랭킹 부여
            if ("NOW_PLAYING".equals(movieInternal.getStatus())) {
                rank = currentRank++;
            }
            
            result.add(MovieListItemDto.fromEntity(
                    movieInternal.getMovie(), 
                    rank, 
                    movieInternal.getReservationRate(), 
                    movieInternal.getReviewRating()
            ));
        }
        
        return result;
    }

    @Getter
    private static class MovieListItemDtoInternal {
        private final Movie movie;
        private final String status;
        private final int statusSortOrder;
        private final Double reservationRate;
        private final Double reviewRating;
        
        public MovieListItemDtoInternal(Movie movie, String status, Double reservationRate, Double reviewRating) {
            this.movie = movie;
            this.status = status;
            this.reservationRate = reservationRate;
            this.reviewRating = reviewRating;
            
            if ("NOW_PLAYING".equals(status)) {
                this.statusSortOrder = 0;
            } else if ("UPCOMING".equals(status)) {
                this.statusSortOrder = 1;
            } else {
                this.statusSortOrder = 2;
            }
        }
    }
}