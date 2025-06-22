package com.uos.picobox.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponseDto {
    private List<MovieSearchItemDto> movies;
    private List<ActorSearchItemDto> actors;
} 