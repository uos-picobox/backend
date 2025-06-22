package com.uos.picobox.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieSearchItemDto {
    private Long movieId;
    private String title;
    private String posterUrl;
    private LocalDate releaseDate;
    private LocalDate screeningEndDate;
    private String movieRatingName;
    private String screeningStatus; // 개봉예정, 상영중, 상영종료
} 