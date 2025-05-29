package com.uos.picobox.domain.movie.service;

import com.uos.picobox.domain.movie.dto.genre.MovieGenreRequestDto;
import com.uos.picobox.domain.movie.dto.genre.MovieGenreResponseDto;
import com.uos.picobox.domain.movie.entity.MovieGenre;
import com.uos.picobox.domain.movie.repository.MovieGenreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

    /**
     * 영화 장르 정보를 삭제합니다.
     * 이 장르를 사용하는 영화가 있다면, 관련 영화 제목 목록과 함께 예외를 발생시킵니다.
     * 강제 삭제 파라미터(force)가 true이면, 관련 영화가 있어도 삭제를 진행합니다 (ON DELETE CASCADE).
     * @param genreId 삭제할 장르 ID
     * @param force 강제 삭제 여부 (true이면 사용 중이어도 삭제)
     * @throws EntityNotFoundException 해당 ID의 장르가 없을 경우
     * @throws IllegalArgumentException 장르가 사용 중이고 force=false일 경우
     * @throws DataIntegrityViolationException DB 레벨의 다른 무결성 제약 조건 위반 시
     */
    @Transactional
    public void removeGenre(Long genreId, boolean force) {
        MovieGenre movieGenre = movieGenreRepository.findById(genreId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 장르를 찾을 수 없습니다: " + genreId));

        List<String> associatedMovies = movieGenreRepository.findMovieTitlesByGenreId(genreId);

        if (!associatedMovies.isEmpty() && !force) {
            String movies = String.join(", ", associatedMovies);
            throw new IllegalArgumentException(
                    String.format("해당 장르를 사용하는 영화가 있어 삭제할 수 없습니다. (장르 ID: %d, 영화: [%s]). 강제 삭제를 원하시면 'force=true' 파라미터를 사용하세요.", genreId, movies)
            );
        }

        // force가 true이거나, associatedMovies가 비어있으면 삭제 진행
        // ON DELETE CASCADE에 의해 MOVIE_GENRE_MAPPING 테이블의 관련 레코드는 자동으로 삭제됩니다.
        try {
            movieGenreRepository.delete(movieGenre);
            log.info("장르가 성공적으로 삭제되었습니다. ID: {}, 강제삭제: {}", genreId, force);
        } catch (DataIntegrityViolationException e) {
            log.error("장르 삭제 중 예상치 못한 DataIntegrityViolationException 발생 (ID: {}): {}", genreId, e.getMessage());
            throw new IllegalStateException("장르 삭제 중 데이터 무결성 문제가 발생했습니다. (ID: " + genreId + ")", e);
        }
    }
}