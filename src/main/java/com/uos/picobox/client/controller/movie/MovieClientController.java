package com.uos.picobox.client.controller.movie;

import com.uos.picobox.domain.movie.dto.movie.MovieListItemDto;
import com.uos.picobox.domain.movie.dto.movie.MovieResponseDto;
import com.uos.picobox.domain.movie.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "사용자 - 영화 정보 조회", description = "사용자 대상 영화 목록 및 상세 정보 조회 API")
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieClientController {

    private final MovieService movieService;

    @Operation(summary = "영화 목록 조회 (현재 상영작 및 개봉 예정작)",
            description = "사용자에게 보여줄 현재 상영작 및 개봉 예정작 목록을 조회합니다. 정렬: 현재 상영작 우선, 그 다음 개봉일 최신순.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영화 목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<MovieListItemDto>> getMovieListForUser() {
        List<MovieListItemDto> movies = movieService.getMovieListForUser();
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "영화 상세 정보 조회", description = "특정 영화의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영화 상세 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 영화를 찾을 수 없습니다.")
    })
    @GetMapping("/{movieId}")
    public ResponseEntity<MovieResponseDto> getMovieDetailForUser(
            @Parameter(description = "조회할 영화의 ID", required = true, example = "1")
            @PathVariable Long movieId) {
        MovieResponseDto movieDetail = movieService.getMovieDetail(movieId);
        return ResponseEntity.ok(movieDetail);
    }
}