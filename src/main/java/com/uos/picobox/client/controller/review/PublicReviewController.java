package com.uos.picobox.client.controller.review;

import com.uos.picobox.domain.review.dto.ReviewResponseDto;
import com.uos.picobox.domain.review.dto.ReviewSummaryDto;
import com.uos.picobox.domain.review.service.ReviewService;
import com.uos.picobox.global.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "04. 공통 - 리뷰 조회", description = "영화 리뷰 조회 기능 (인증 불필요)")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class PublicReviewController {

    private final ReviewService reviewService;
    private final SessionUtils sessionUtils;

    @Operation(summary = "영화별 리뷰 목록 조회", description = "특정 영화의 모든 리뷰를 페이징으로 조회합니다. (인증 선택사항)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공"),
            @ApiResponse(responseCode = "204", description = "리뷰 없음")
    })
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByMovie(
            @PathVariable Long movieId,
            @Parameter(description = "정렬 기준 (latest: 최신순, like: 좋아요순)", example = "latest")
            @RequestParam(defaultValue = "latest") String sortBy,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String sessionId,
            Authentication authentication) {
        
        // 인증된 사용자인 경우 좋아요 정보를 포함하여 조회
        Long userId = null;
        try {
            if (authentication != null) {
                userId = sessionUtils.findCustomerIdByAuthentication(authentication);
            }
        } catch (Exception e) {
            // 인증 실패 시 null로 처리 (비회원으로 조회)
        }
        
        Page<ReviewResponseDto> reviews = reviewService.getReviewsByMovie(movieId, sortBy, page, size, userId);
        
        if (reviews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "영화 리뷰 요약 정보", description = "특정 영화의 평균 평점과 총 리뷰 개수를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 요약 정보 조회 성공")
    })
    @GetMapping("/movie/{movieId}/summary")
    public ResponseEntity<ReviewSummaryDto> getReviewSummary(@PathVariable Long movieId) {
        ReviewSummaryDto summary = reviewService.getReviewSummary(movieId);
        return ResponseEntity.ok(summary);
    }
} 