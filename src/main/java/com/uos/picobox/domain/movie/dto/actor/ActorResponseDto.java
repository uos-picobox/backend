package com.uos.picobox.domain.movie.dto.actor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uos.picobox.domain.movie.entity.Actor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ActorResponseDto {

    @Schema(description = "배우 ID", example = "1")
    private Long actorId;

    @Schema(description = "배우 이름", example = "송강호")
    private String name;

    @Schema(description = "생년월일 (yyyy-MM-dd)", example = "1967-01-17")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(description = "배우 소개", example = "배우입니다.")
    private String biography;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;

    public ActorResponseDto(Actor actor) {
        this.actorId = actor.getId();
        this.name = actor.getName();
        this.birthDate = actor.getBirthDate();
        this.biography = actor.getBiography();
        this.profileImageUrl = actor.getProfileImageUrl();
    }
}