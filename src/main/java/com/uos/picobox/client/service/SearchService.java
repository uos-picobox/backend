package com.uos.picobox.client.service;

import com.uos.picobox.client.dto.ActorSearchItemDto;
import com.uos.picobox.client.dto.MovieSearchItemDto;
import com.uos.picobox.client.dto.SearchResponseDto;
import com.uos.picobox.domain.movie.entity.Actor;
import com.uos.picobox.domain.movie.entity.Movie;
import com.uos.picobox.domain.movie.repository.ActorRepository;
import com.uos.picobox.domain.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;

    public SearchResponseDto search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return SearchResponseDto.builder()
                    .movies(Collections.emptyList())
                    .actors(Collections.emptyList())
                    .build();
        }

        String trimmedKeyword = keyword.trim();
        
        // 영화 검색 (제목으로 검색 + 배우 이름으로 검색)
        List<Movie> moviesByTitle = movieRepository.findMoviesByTitleContaining(trimmedKeyword);
        List<Movie> moviesByActor = movieRepository.findMoviesByActorNameContaining(trimmedKeyword);
        
        Set<Movie> allMovies = new HashSet<>(moviesByTitle);
        allMovies.addAll(moviesByActor);
        
        List<MovieSearchItemDto> movieSearchItems = allMovies.stream()
                .map(this::convertToMovieSearchItemDto)
                .sorted(this::compareMoviesByScreeningStatus)
                .collect(Collectors.toList());

        // 배우 검색
        List<Actor> actors = actorRepository.findActorsByNameContaining(trimmedKeyword);
        List<ActorSearchItemDto> actorSearchItems = actors.stream()
                .map(this::convertToActorSearchItemDto)
                .collect(Collectors.toList());

        return SearchResponseDto.builder()
                .movies(movieSearchItems)
                .actors(actorSearchItems)
                .build();
    }

    private MovieSearchItemDto convertToMovieSearchItemDto(Movie movie) {
        LocalDate today = LocalDate.now();
        String screeningStatus = determineScreeningStatus(
                movie.getReleaseDate(), 
                movie.getScreeningEndDate(), 
                today
        );

        return MovieSearchItemDto.builder()
                .movieId(movie.getId())
                .title(movie.getTitle())
                .posterUrl(movie.getPosterUrl())
                .releaseDate(movie.getReleaseDate())
                .screeningEndDate(movie.getScreeningEndDate())
                .movieRatingName(movie.getMovieRating().getRatingName())
                .screeningStatus(screeningStatus)
                .build();
    }

    private ActorSearchItemDto convertToActorSearchItemDto(Actor actor) {
        return ActorSearchItemDto.builder()
                .actorId(actor.getId())
                .name(actor.getName())
                .profileImageUrl(actor.getProfileImageUrl())
                .build();
    }

    private String determineScreeningStatus(LocalDate releaseDate, LocalDate screeningEndDate, LocalDate today) {
        if (releaseDate.isAfter(today)) {
            return "개봉예정";
        } else if (screeningEndDate == null || screeningEndDate.isAfter(today) || screeningEndDate.isEqual(today)) {
            return "상영중";
        } else {
            return "상영종료";
        }
    }

    private int compareMoviesByScreeningStatus(MovieSearchItemDto a, MovieSearchItemDto b) {
        // 상영 상태별 우선순위: 개봉예정(1) > 상영중(2) > 상영종료(3)
        int priorityA = getScreeningStatusPriority(a.getScreeningStatus());
        int priorityB = getScreeningStatusPriority(b.getScreeningStatus());
        
        if (priorityA != priorityB) {
            return Integer.compare(priorityA, priorityB);
        }
        
        // 같은 상태 내에서는 개봉일 최신순 (내림차순)
        return b.getReleaseDate().compareTo(a.getReleaseDate());
    }

    private int getScreeningStatusPriority(String status) {
        switch (status) {
            case "개봉예정": return 1;
            case "상영중": return 2;
            case "상영종료": return 3;
            default: return 4;
        }
    }
} 