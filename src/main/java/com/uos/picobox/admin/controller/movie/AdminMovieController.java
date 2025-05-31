package com.uos.picobox.admin.controller.movie;

import com.uos.picobox.domain.movie.dto.movie.MovieRequestDto;
import com.uos.picobox.domain.movie.dto.movie.MovieResponseDto;
import com.uos.picobox.domain.movie.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "관리자 - 05. 영화 정보 관리", description = "영화 정보 CRUD API (관리자용)")
@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final MovieService movieService;

    @Operation(summary = "영화 정보 및 포스터 동시 등록",
            description = "새로운 영화 정보(JSON 'movieDetails')와 포스터 이미지 파일('posterImage')을 한 번의 요청으로 등록합니다. Content-Type은 'multipart/form-data'여야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "영화 정보와 이미지가 성공적으로 등록되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovieResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복 영화, 파일 형식 오류 등)."),
            @ApiResponse(responseCode = "404", description = "관련 정보(배급사, 등급, 장르, 배우)를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류 또는 파일 업로드 실패입니다.")
    })
    @PostMapping(value = "/create-with-image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<MovieResponseDto> createMovieWithImage(
            @Parameter(name = "movieDetails", required = true, description = "영화 상세 정보 (JSON 형식)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovieRequestDto.class)))
            @Valid @RequestPart("movieDetails") MovieRequestDto requestDto,

            @Parameter(name = "posterImage", description = "포스터 이미지 파일 (선택 사항)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "posterImage", required = false) MultipartFile posterImageFile) {
        MovieResponseDto responseDto = movieService.registerMovie(requestDto, posterImageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "영화 정보 등록 (메타데이터 전용)",
            description = "새로운 영화 정보(JSON)만 시스템에 등록합니다. 포스터 이미지는 null로 설정되며, '/{movieId}/poster' API로 별도 업로드해야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "영화 정보(메타데이터)가 성공적으로 등록되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovieResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복 영화)."),
            @ApiResponse(responseCode = "404", description = "관련 정보(배급사, 등급, 장르, 배우)를 찾을 수 없습니다.")
    })
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<MovieResponseDto> createMovieMetadata(
            @Valid @RequestBody MovieRequestDto requestDto) {
        MovieResponseDto responseDto = movieService.registerMovie(requestDto, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "영화 정보 및 포스터 동시 수정",
            description = "기존 영화 정보(JSON 'movieDetails')를 수정하고, 포스터 이미지 파일('posterImage')도 함께 교체합니다. Content-Type은 'multipart/form-data'여야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영화 정보와 이미지가 성공적으로 수정되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovieResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복 영화, 파일 형식 오류 등)."),
            @ApiResponse(responseCode = "404", description = "해당 영화 또는 관련 정보를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류 또는 파일 업로드 실패입니다.")
    })
    @PutMapping(value = "/{movieId}/update-with-image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<MovieResponseDto> updateMovieWithImage(
            @Parameter(description = "수정할 영화 ID", required = true) @PathVariable Long movieId,
            @Parameter(name = "movieDetails", required = true, description = "수정할 영화 상세 정보 (JSON 형식)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovieRequestDto.class)))
            @Valid @RequestPart("movieDetails") MovieRequestDto requestDto,
            @Parameter(name = "posterImage", description = "새 포스터 이미지 파일 (선택 사항, 기존 이미지 교체)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "posterImage", required = false) MultipartFile posterImageFile) {
        MovieResponseDto updatedMovie = movieService.editMovie(movieId, requestDto, posterImageFile);
        return ResponseEntity.ok(updatedMovie);
    }

    @Operation(summary = "영화 정보 수정 (메타데이터 전용)",
            description = "기존 영화 정보(JSON)만 수정합니다. 포스터 이미지는 이 API로 변경되지 않으며, '/{movieId}/poster' API를 사용해야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영화 정보(메타데이터)가 성공적으로 수정되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovieResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복 영화)."),
            @ApiResponse(responseCode = "404", description = "해당 영화를 찾을 수 없습니다.")
    })
    @PutMapping(value = "/{movieId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<MovieResponseDto> updateMovieMetadata(
            @Parameter(description = "수정할 영화 ID", required = true) @PathVariable Long movieId,
            @Valid @RequestBody MovieRequestDto requestDto) {
        MovieResponseDto updatedMovie = movieService.editMovie(movieId, requestDto, null);
        return ResponseEntity.ok(updatedMovie);
    }

    @Operation(summary = "영화 포스터 이미지 업로드/변경/삭제 (이미지 전용)",
            description = "특정 영화의 포스터 이미지를 업로드하거나 변경합니다. 파일을 보내면 기존 이미지는 삭제 후 새 이미지로 교체됩니다. 파일을 보내지 않으면(null 또는 empty) 기존 포스터가 삭제되고 URL이 null로 설정됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포스터 이미지가 성공적으로 업로드/변경/삭제되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovieResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 영화를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "파일 처리 중 오류가 발생했습니다.")
    })
    @PutMapping(value = "/{movieId}/poster", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<MovieResponseDto> uploadOrUpdateMoviePoster(
            @Parameter(description = "포스터를 업로드/변경/삭제할 영화 ID", required = true) @PathVariable Long movieId,
            @Parameter(name = "posterImage", description = "포스터 이미지 파일. 파일을 보내지 않으면 기존 포스터가 삭제됩니다.",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "posterImage", required = false) MultipartFile posterImageFile) {
        MovieResponseDto movieResponseDto = movieService.uploadMoviePoster(movieId, posterImageFile);
        return ResponseEntity.ok(movieResponseDto);
    }

    @Operation(summary = "영화 전체 목록 조회", description = "시스템에 등록된 모든 영화 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영화 목록이 성공적으로 조회되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovieResponseDto.class)))
    })
    @GetMapping("/get")
    public ResponseEntity<List<MovieResponseDto>> getAllMovies() {
        List<MovieResponseDto> movies = movieService.findAllMovies();
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "특정 영화 정보 조회", description = "ID로 특정 영화의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "영화 정보가 성공적으로 조회되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MovieResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 영화를 찾을 수 없습니다.")
    })
    @GetMapping("/get/{movieId}")
    public ResponseEntity<MovieResponseDto> getMovieById(
            @Parameter(description = "조회할 영화 ID", required = true) @PathVariable Long movieId) {
        MovieResponseDto movie = movieService.findMovieById(movieId);
        return ResponseEntity.ok(movie);
    }

    @Operation(summary = "영화 정보 삭제", description = "영화 정보를 시스템에서 삭제합니다. S3의 포스터 이미지도 함께 삭제됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "영화 정보가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 영화를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 상영 스케줄이 있는 영화 등).")
    })
    @DeleteMapping("/delete/{movieId}")
    public ResponseEntity<Void> deleteMovie(
            @Parameter(description = "삭제할 영화 ID", required = true) @PathVariable Long movieId) {
        movieService.removeMovie(movieId);
        return ResponseEntity.noContent().build();
    }
}