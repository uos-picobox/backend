package com.uos.picobox.admin.controller.movie;

import com.uos.picobox.domain.movie.dto.rating.MovieRatingRequestDto;
import com.uos.picobox.domain.movie.dto.rating.MovieRatingResponseDto;
import com.uos.picobox.domain.movie.service.MovieRatingService;
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

@Tag(name = "관리자 - 04. 영화 등급 관리", description = "영화 등급 정보 CRUD API (관리자용)")
@RestController
@RequestMapping("/api/admin/movie-ratings")
@RequiredArgsConstructor
public class AdminMovieRatingController {

    private final MovieRatingService movieRatingService;

    @Operation(summary = "영화 등급 등록", description = "새로운 영화 등급을 시스템에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "영화 등급이 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복된 등급명)."),
    })
    @PostMapping("/create")
    public ResponseEntity<MovieRatingResponseDto> createMovieRating(
            @Valid @RequestBody MovieRatingRequestDto requestDto) {
        MovieRatingResponseDto responseDto = movieRatingService.registerMovieRating(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "영화 등급 전체 목록 조회", description = "등록된 모든 영화 등급 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영화 등급 목록이 성공적으로 조회되었습니다."),
    })
    @GetMapping("/get")
    public ResponseEntity<List<MovieRatingResponseDto>> getAllMovieRatings() {
        List<MovieRatingResponseDto> movieRatings = movieRatingService.findAllMovieRatings();
        return ResponseEntity.ok(movieRatings);
    }

    @Operation(summary = "특정 영화 등급 조회", description = "ID로 특정 영화 등급 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영화 등급 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 영화 등급을 찾을 수 없습니다."),
    })
    @GetMapping("/get/{ratingId}")
    public ResponseEntity<MovieRatingResponseDto> getMovieRatingById(
            @Parameter(description = "조회할 영화 등급 ID", required = true, example = "1")
            @PathVariable Long ratingId) {
        MovieRatingResponseDto movieRating = movieRatingService.findMovieRatingById(ratingId);
        return ResponseEntity.ok(movieRating);
    }

    @Operation(summary = "영화 등급 수정", description = "기존 영화 등급 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영화 등급 정보가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복된 등급명)."),
            @ApiResponse(responseCode = "404", description = "해당 영화 등급을 찾을 수 없습니다."),
    })
    @PutMapping("/update/{ratingId}")
    public ResponseEntity<MovieRatingResponseDto> updateMovieRating(
            @Parameter(description = "수정할 영화 등급 ID", required = true, example = "1")
            @PathVariable Long ratingId,
            @Valid @RequestBody MovieRatingRequestDto requestDto) {
        MovieRatingResponseDto updatedMovieRating = movieRatingService.editMovieRating(ratingId, requestDto);
        return ResponseEntity.ok(updatedMovieRating);
    }

    @Operation(summary = "영화 등급 삭제", description = "영화 등급 정보를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "영화 등급 정보가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 영화 등급을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 해당 등급을 사용하는 영화가 있는 경우)."),
    })
    @DeleteMapping("/delete/{ratingId}")
    public ResponseEntity<Void> deleteMovieRating(
            @Parameter(description = "삭제할 영화 등급 ID", required = true, example = "1")
            @PathVariable Long ratingId) {
        movieRatingService.removeMovieRating(ratingId);
        return ResponseEntity.noContent().build();
    }
}