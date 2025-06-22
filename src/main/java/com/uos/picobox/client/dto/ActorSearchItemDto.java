package com.uos.picobox.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorSearchItemDto {
    private Long actorId;
    private String name;
    private String profileImageUrl;
} 