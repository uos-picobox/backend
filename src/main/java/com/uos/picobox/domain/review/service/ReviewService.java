package com.uos.picobox.domain.review.service;

import com.uos.picobox.domain.movie.entity.Movie;
import com.uos.picobox.domain.movie.repository.MovieRepository;
import com.uos.picobox.domain.reservation.entity.Reservation;
import com.uos.picobox.global.enumClass.TicketStatus;
import com.uos.picobox.domain.reservation.repository.ReservationRepository;
import com.uos.picobox.domain.screening.entity.Screening;
import com.uos.picobox.domain.screening.repository.ScreeningRepository;
import com.uos.picobox.domain.review.dto.ReviewRequestDto;
import com.uos.picobox.domain.review.dto.ReviewResponseDto;
import com.uos.picobox.domain.review.dto.ReviewSummaryDto;
import com.uos.picobox.domain.review.entity.Review;
import com.uos.picobox.domain.review.entity.ReviewLike;
import com.uos.picobox.domain.review.repository.ReviewLikeRepository;
import com.uos.picobox.domain.review.repository.ReviewRepository;
import com.uos.picobox.user.entity.Customer;
import com.uos.picobox.user.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;

    /**
     * 리뷰 작성 - 관람 완료한 영화에 대해서만 작성 가능
     */
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto dto, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("고객 정보를 찾을 수 없습니다: " + customerId));

        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new EntityNotFoundException("영화 정보를 찾을 수 없습니다: " + dto.getMovieId()));

        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new EntityNotFoundException("예매 정보를 찾을 수 없습니다: " + dto.getReservationId()));

        // 예매자가 본인인지 확인
        if (!reservation.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("본인의 예매에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        // 관람 완료 확인 (상영 시작 후 10분이 지났는지 확인 또는 티켓이 USED 상태인지 확인)
        Screening screening = reservation.getTickets().isEmpty() ? null : 
            screeningRepository.findById(reservation.getTickets().get(0).getScreeningId()).orElse(null);
        
        if (screening == null) {
            throw new IllegalStateException("상영 정보를 찾을 수 없습니다.");
        }
        
        // 티켓이 USED 상태이거나 상영 시작 후 10분이 지났으면 리뷰 작성 가능
        boolean hasUsedTicket = reservation.getTickets().stream()
                .anyMatch(ticket -> ticket.getTicketStatus() == TicketStatus.USED);
        
        LocalDateTime screeningStartPlus10Min = screening.getScreeningTime().plusMinutes(10);
        boolean screeningStarted = LocalDateTime.now().isAfter(screeningStartPlus10Min);
        
        if (!hasUsedTicket && !screeningStarted) {
            throw new IllegalStateException("상영 시작 후 10분이 지난 후에 리뷰를 작성할 수 있습니다.");
        }

        // 이미 해당 영화에 대한 리뷰가 있는지 확인 (한 영화당 하나의 리뷰)
        if (reviewRepository.existsByCustomerIdAndMovieId(customerId, dto.getMovieId())) {
            throw new IllegalStateException("이미 해당 영화에 대한 리뷰를 작성하셨습니다. 기존 리뷰를 수정해주세요.");
        }

        // 평점 유효성 검사 (0.5 단위)
        if (!isValidRating(dto.getRating())) {
            throw new IllegalArgumentException("평점은 0.5 단위로 0.5~5.0 사이의 값이어야 합니다.");
        }

        Review review = Review.builder()
                .reservation(reservation)
                .customer(customer)
                .movie(movie)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("리뷰 작성 완료: reviewId={}, customerId={}, movieId={}", savedReview.getId(), customerId, dto.getMovieId());

        return new ReviewResponseDto(savedReview);
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto dto, Long customerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId));

        // 작성자 본인인지 확인
        if (!review.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        // 평점 유효성 검사
        if (!isValidRating(dto.getRating())) {
            throw new IllegalArgumentException("평점은 0.5 단위로 0.5~5.0 사이의 값이어야 합니다.");
        }

        review.updateReview(dto.getRating(), dto.getComment());
        log.info("리뷰 수정 완료: reviewId={}, customerId={}", reviewId, customerId);

        return new ReviewResponseDto(review);
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public void deleteReview(Long reviewId, Long customerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId));

        // 작성자 본인인지 확인
        if (!review.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
        log.info("리뷰 삭제 완료: reviewId={}, customerId={}", reviewId, customerId);
    }

    /**
     * 특정 영화의 리뷰 목록 조회 (페이징, 정렬)
     */
    public Page<ReviewResponseDto> getReviewsByMovie(Long movieId, String sortBy, int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews;

        if ("like".equals(sortBy)) {
            reviews = reviewRepository.findByMovieIdOrderByLikeCountDesc(movieId, pageable);
        } else {
            reviews = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId, pageable);
        }

        return reviews.map(review -> {
            boolean isLiked = currentUserId != null && 
                    reviewLikeRepository.existsByReviewIdAndCustomerId(review.getId(), currentUserId);
            return new ReviewResponseDto(review, isLiked);
        });
    }

    /**
     * 영화별 리뷰 요약 정보 조회 (평균 평점, 총 리뷰 수)
     */
    public ReviewSummaryDto getReviewSummary(Long movieId) {
        Double averageRating = reviewRepository.calculateAverageRatingByMovieId(movieId);
        Long totalReviews = reviewRepository.countByMovieId(movieId);
        
        return new ReviewSummaryDto(movieId, averageRating, totalReviews);
    }

    /**
     * 리뷰 좋아요 토글 (추가/제거)
     */
    @Transactional
    public boolean toggleReviewLike(Long reviewId, Long customerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("고객 정보를 찾을 수 없습니다: " + customerId));

        boolean alreadyLiked = reviewLikeRepository.existsByReviewIdAndCustomerId(reviewId, customerId);

        if (alreadyLiked) {
            // 좋아요 제거
            reviewLikeRepository.deleteByReviewIdAndCustomerId(reviewId, customerId);
            review.decreaseLikeCount();
            log.info("리뷰 좋아요 제거: reviewId={}, customerId={}", reviewId, customerId);
            return false;
        } else {
            // 좋아요 추가
            ReviewLike reviewLike = ReviewLike.builder()
                    .review(review)
                    .customer(customer)
                    .build();
            reviewLikeRepository.save(reviewLike);
            review.increaseLikeCount();
            log.info("리뷰 좋아요 추가: reviewId={}, customerId={}", reviewId, customerId);
            return true;
        }
    }

    /**
     * 내가 작성한 리뷰 목록 조회
     */
    public Page<ReviewResponseDto> getMyReviews(Long customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        
        return reviews.map(review -> new ReviewResponseDto(review, false));
    }

    /**
     * 평점 유효성 검사 (0.5 단위)
     */
    private boolean isValidRating(Double rating) {
        if (rating == null || rating < 0.5 || rating > 5.0) {
            return false;
        }
        // 0.5 단위 체크 (예: 1.0, 1.5, 2.0, 2.5, ...)
        return (rating * 2) % 1 == 0;
    }

    // ===== 관리자용 메서드들 =====

    /**
     * 관리자용 전체 리뷰 목록 조회
     */
    public Page<ReviewResponseDto> getAllReviewsForAdmin(int page, int size, String sort) {
        Pageable pageable;
        
        if ("oldest".equals(sort)) {
            pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        } else {
            // 기본값: latest (최신순)
            pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        }
        
        Page<Review> reviews = reviewRepository.findAll(pageable);
        return reviews.map(review -> new ReviewResponseDto(review, false));
    }

    /**
     * 관리자용 특정 영화 리뷰 목록 조회
     */
    public Page<ReviewResponseDto> getReviewsByMovieForAdmin(Long movieId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews;
        
        if ("like".equals(sort)) {
            reviews = reviewRepository.findByMovieIdOrderByLikeCountDesc(movieId, pageable);
        } else {
            // 기본값: latest (최신순)
            reviews = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId, pageable);
        }
        
        return reviews.map(review -> new ReviewResponseDto(review, false));
    }

    /**
     * 관리자용 리뷰 상세 조회
     */
    public ReviewResponseDto getReviewDetailForAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId));
        
        return new ReviewResponseDto(review, false);
    }

    /**
     * 관리자용 리뷰 삭제 (작성자 확인 없이)
     */
    @Transactional
    public void deleteReviewByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId));
        
        reviewRepository.delete(review);
        log.info("관리자에 의한 리뷰 삭제 완료: reviewId={}, 작성자={}", reviewId, review.getCustomer().getName());
    }

    /**
     * 관리자용 특정 고객 리뷰 목록 조회
     */
    public Page<ReviewResponseDto> getReviewsByCustomerForAdmin(Long customerId, int page, int size) {
        // 고객 존재 여부 확인
        customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("고객 정보를 찾을 수 없습니다: " + customerId));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        
        return reviews.map(review -> new ReviewResponseDto(review, false));
    }
} 