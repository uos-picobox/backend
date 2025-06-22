package com.uos.picobox.admin.controller.review;

import com.uos.picobox.domain.review.dto.ReviewResponseDto;
import com.uos.picobox.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 - 10. 리뷰 관리", description = "관리자용 리뷰 조회 및 삭제 기능")
@Slf4j
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "전체 리뷰 목록 조회", description = "모든 리뷰를 페이징으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요")
    })
    @GetMapping
    public ResponseEntity<Page<ReviewResponseDto>> getAllReviews(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 (latest: 최신순, oldest: 오래된순)", example = "latest")
            @RequestParam(defaultValue = "latest") String sort) {
        
        log.info("관리자 전체 리뷰 조회 요청: page={}, size={}, sort={}", page, size, sort);
        Page<ReviewResponseDto> reviews = reviewService.getAllReviewsForAdmin(page, size, sort);
        
        if (reviews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "특정 영화 리뷰 목록 조회", description = "특정 영화의 모든 리뷰를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "404", description = "영화 정보 없음")
    })
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByMovie(
            @Parameter(description = "영화 ID", required = true) @PathVariable Long movieId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 (latest: 최신순, like: 좋아요순)", example = "latest")
            @RequestParam(defaultValue = "latest") String sort) {
        
        log.info("관리자 영화별 리뷰 조회 요청: movieId={}, page={}, size={}, sort={}", movieId, page, size, sort);
        Page<ReviewResponseDto> reviews = reviewService.getReviewsByMovieForAdmin(movieId, page, size, sort);
        
        if (reviews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "리뷰 상세 조회", description = "특정 리뷰를 상세 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReviewDetail(
            @Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId) {
        
        log.info("관리자 리뷰 상세 조회 요청: reviewId={}", reviewId);
        ReviewResponseDto review = reviewService.getReviewDetailForAdmin(reviewId);
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "리뷰 삭제 (관리자)", description = "관리자 권한으로 특정 리뷰를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음")
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId) {
        
        log.info("관리자 리뷰 삭제 요청: reviewId={}", reviewId);
        reviewService.deleteReviewByAdmin(reviewId);
        log.info("관리자 리뷰 삭제 완료: reviewId={}", reviewId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "특정 고객의 리뷰 목록 조회", description = "특정 고객이 작성한 모든 리뷰를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "404", description = "고객 정보 없음")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByCustomer(
            @Parameter(description = "고객 ID", required = true) @PathVariable Long customerId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("관리자 고객별 리뷰 조회 요청: customerId={}, page={}, size={}", customerId, page, size);
        Page<ReviewResponseDto> reviews = reviewService.getReviewsByCustomerForAdmin(customerId, page, size);
        
        if (reviews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reviews);
    }
} 