package com.uos.picobox.domain.review.entity;

import com.uos.picobox.domain.movie.entity.Movie;
import com.uos.picobox.domain.reservation.entity.Reservation;
import com.uos.picobox.user.entity.Customer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "REVIEW")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REVIEW_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESERVATION_ID", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOVIE_ID", nullable = false)
    private Movie movie;

    @Column(name = "RATING", nullable = false)
    private Double rating;

    @Column(name = "REVIEW_COMMENT", nullable = false, length = 500)
    private String comment;

    @Column(name = "LIKE_COUNT", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewLike> reviewLikes = new ArrayList<>();

    @Builder
    public Review(Reservation reservation, Customer customer, Movie movie, Double rating, String comment) {
        this.reservation = reservation;
        this.customer = customer;
        this.movie = movie;
        this.rating = rating;
        this.comment = comment;
        this.likeCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public void updateReview(Double rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.likeCount == null) {
            this.likeCount = 0;
        }
    }
} 