package com.uos.picobox.admin.controller.movie;

import com.uos.picobox.domain.movie.dto.movie.MovieRequestDto;
import com.uos.picobox.domain.movie.dto.movie.MovieResponseDto;
import com.uos.picobox.domain.movie.service.MovieService;
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

@Tag(name = "관리자 - 05. 영화 정보 관리", description = "영화 정보 CRUD API (관리자용)")
@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final MovieService movieService;

    @Operation(summary = "영화 정보 등록", description = "새로운 영화 정보를 시스템에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "영화 정보가 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복 영화)."),
            @ApiResponse(responseCode = "404", description = "관련 정보(배급사, 등급, 장르, 배우)를 찾을 수 없습니다.")
    })
    @PostMapping("/create")
    public ResponseEntity<MovieResponseDto> createMovie(
            @Valid @RequestBody MovieRequestDto requestDto) {
        MovieResponseDto responseDto = movieService.registerMovie(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "영화 전체 목록 조회", description = "등록된 모든 영화 목록을 조회합니다.")
    @GetMapping("/get")
    public ResponseEntity<List<MovieResponseDto>> getAllMovies() {
        List<MovieResponseDto> movies = movieService.findAllMovies();
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "특정 영화 정보 조회", description = "ID로 특정 영화의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영화 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 영화를 찾을 수 없습니다."),
    })
    @GetMapping("/get/{movieId}")
    public ResponseEntity<MovieResponseDto> getMovieById(
            @Parameter(description = "조회할 영화 ID", required = true, example = "1")
            @PathVariable Long movieId) {
        MovieResponseDto movie = movieService.findMovieById(movieId);
        return ResponseEntity.ok(movie);
    }

    @Operation(summary = "영화 정보 수정", description = "기존 영화 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영화 정보가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복 영화)."),
            @ApiResponse(responseCode = "404", description = "해당 영화 또는 관련 정보를 찾을 수 없습니다.")
    })
    @PutMapping("/update/{movieId}")
    public ResponseEntity<MovieResponseDto> updateMovie(
            @Parameter(description = "수정할 영화 ID", required = true, example = "1")
            @PathVariable Long movieId,
            @Valid @RequestBody MovieRequestDto requestDto) {
        MovieResponseDto updatedMovie = movieService.editMovie(movieId, requestDto);
        return ResponseEntity.ok(updatedMovie);
    }

    @Operation(summary = "영화 정보 삭제", description = "영화 정보를 시스템에서 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "영화 정보가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 영화를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 상영 중인 영화).")
    })
    @DeleteMapping("/delete/{movieId}")
    public ResponseEntity<Void> deleteMovie(
            @Parameter(description = "삭제할 영화 ID", required = true, example = "1")
            @PathVariable Long movieId) {
        movieService.removeMovie(movieId);
        return ResponseEntity.noContent().build();
    }
}