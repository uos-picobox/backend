package com.uos.picobox.domain.review.repository;

import com.uos.picobox.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 특정 고객이 특정 영화에 대해 작성한 리뷰 조회 (한 영화당 하나의 리뷰)
     */
    Optional<Review> findByCustomerIdAndMovieId(Long customerId, Long movieId);

    /**
     * 특정 고객이 특정 영화에 대해 리뷰를 작성했는지 확인
     */
    boolean existsByCustomerIdAndMovieId(Long customerId, Long movieId);

    /**
     * 특정 영화의 모든 리뷰를 페이징으로 조회 (최신순)
     */
    @Query("SELECT r FROM Review r WHERE r.movie.id = :movieId ORDER BY r.createdAt DESC")
    Page<Review> findByMovieIdOrderByCreatedAtDesc(@Param("movieId") Long movieId, Pageable pageable);

    /**
     * 특정 영화의 모든 리뷰를 페이징으로 조회 (좋아요순)
     */
    @Query("SELECT r FROM Review r WHERE r.movie.id = :movieId ORDER BY r.likeCount DESC, r.createdAt DESC")
    Page<Review> findByMovieIdOrderByLikeCountDesc(@Param("movieId") Long movieId, Pageable pageable);

    /**
     * 특정 영화의 평균 평점 계산
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movie.id = :movieId")
    Double calculateAverageRatingByMovieId(@Param("movieId") Long movieId);

    /**
     * 특정 영화의 총 리뷰 개수
     */
    long countByMovieId(Long movieId);

    /**
     * 특정 고객이 작성한 모든 리뷰 조회 (최신순)
     */
    @Query("SELECT r FROM Review r WHERE r.customer.id = :customerId ORDER BY r.createdAt DESC")
    Page<Review> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId, Pageable pageable);
} 