package com.uos.picobox.admin.Controller;

import com.uos.picobox.domain.movie.dto.genre.MovieGenreRequestDto;
import com.uos.picobox.domain.movie.dto.genre.MovieGenreResponseDto;
import com.uos.picobox.domain.movie.service.MovieGenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자 - 01. 영화 장르 관리", description = "영화 장르 정보 CRUD API (관리자용)")
@RestController
@RequestMapping("/api/admin/movie-genres")
@RequiredArgsConstructor
public class AdminMovieGenreController {

    private final MovieGenreService movieGenreService;

    @Operation(summary = "영화 장르 등록", description = "새로운 영화 장르를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "장르가 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복된 장르명)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("/create")
    public ResponseEntity<MovieGenreResponseDto> createMovieGenre(
            @Valid @RequestBody MovieGenreRequestDto movieGenreRequestDto) {
        MovieGenreResponseDto responseDto = movieGenreService.registerGenre(movieGenreRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "영화 장르 전체 목록 조회", description = "등록된 모든 영화 장르 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장르 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @GetMapping("/get")
    public ResponseEntity<List<MovieGenreResponseDto>> getAllMovieGenres() {
        List<MovieGenreResponseDto> genres = movieGenreService.findAllGenres();
        return ResponseEntity.ok(genres);
    }

    @Operation(summary = "특정 영화 장르 조회", description = "ID로 특정 영화 장르 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장르 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 장르를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @GetMapping("/get/{genreId}")
    public ResponseEntity<MovieGenreResponseDto> getMovieGenreById(
            @Parameter(description = "조회할 장르 ID", required = true, example = "1")
            @PathVariable Long genreId) {
        MovieGenreResponseDto genre = movieGenreService.findGenreById(genreId);
        return ResponseEntity.ok(genre);
    }

    @Operation(summary = "영화 장르 수정", description = "기존 영화 장르 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장르 정보가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복된 장르명)."),
            @ApiResponse(responseCode = "404", description = "해당 장르를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PutMapping("/update/{genreId}")
    public ResponseEntity<MovieGenreResponseDto> updateMovieGenre(
            @Parameter(description = "수정할 장르 ID", required = true, example = "1")
            @PathVariable Long genreId,
            @Valid @RequestBody MovieGenreRequestDto movieGenreRequestDto) {
        MovieGenreResponseDto updatedGenre = movieGenreService.editGenre(genreId, movieGenreRequestDto);
        return ResponseEntity.ok(updatedGenre);
    }

    @Operation(summary = "영화 장르 삭제", description = "영화 장르 정보를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "장르 정보가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 장르를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 해당 장르를 사용하는 영화가 있는 경우)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @DeleteMapping("/delete/{genreId}")
    public ResponseEntity<Void> deleteMovieGenre(
            @Parameter(description = "삭제할 장르 ID", required = true, example = "1")
            @PathVariable Long genreId) {
        movieGenreService.removeGenre(genreId);
        return ResponseEntity.noContent().build();
    }
}