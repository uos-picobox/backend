package com.uos.picobox.client.controller.review;

import com.uos.picobox.domain.review.dto.ReviewRequestDto;
import com.uos.picobox.domain.review.dto.ReviewResponseDto;
import com.uos.picobox.domain.review.dto.ReviewSummaryDto;
import com.uos.picobox.domain.review.service.ReviewService;
import com.uos.picobox.global.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "05. 회원 - 리뷰 관리", description = "영화 리뷰 작성, 조회, 수정, 삭제 및 좋아요 기능")
@RestController
@RequestMapping("/api/protected/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final SessionUtils sessionUtils;

    @Operation(summary = "리뷰 작성", description = "관람 완료한 영화에 대해 리뷰를 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (관람 미완료, 중복 리뷰 등)"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "예매 또는 영화 정보 없음")
    })
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @Valid @RequestBody ReviewRequestDto dto,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Long id = sessionUtils.findCustomerIdByAuthentication(authentication);
        ReviewResponseDto review = reviewService.createReview(dto, id);
        return ResponseEntity.status(201).body(review);
    }

    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음")
    })
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequestDto dto,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Long id = sessionUtils.findCustomerIdByAuthentication(authentication);
        ReviewResponseDto review = reviewService.updateReview(reviewId, dto, id);
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음")
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Long id = sessionUtils.findCustomerIdByAuthentication(authentication);
        reviewService.deleteReview(reviewId, id);
        return ResponseEntity.noContent().build();
    }



    @Operation(summary = "리뷰 좋아요 토글", description = "리뷰에 좋아요를 추가하거나 제거합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 토글 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "리뷰 없음")
    })
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<Map<String, Object>> toggleReviewLike(
            @PathVariable Long reviewId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Long id = sessionUtils.findCustomerIdByAuthentication(authentication);
        boolean isLiked = reviewService.toggleReviewLike(reviewId, id);
        
        return ResponseEntity.ok(Map.of(
                "isLiked", isLiked,
                "message", isLiked ? "좋아요를 추가했습니다." : "좋아요를 취소했습니다."
        ));
    }

    @Operation(summary = "내가 작성한 리뷰 목록", description = "로그인한 사용자가 작성한 모든 리뷰를 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<Page<ReviewResponseDto>> getMyReviews(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Long id = sessionUtils.findCustomerIdByAuthentication(authentication);
        Page<ReviewResponseDto> reviews = reviewService.getMyReviews(id, page, size);
        
        if (reviews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reviews);
    }
} 