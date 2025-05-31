package com.uos.picobox.domain.movie.dto.actor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ActorRequestDto {

    @NotBlank(message = "배우 이름은 필수 입력 항목입니다.")
    @Size(max = 255, message = "배우 이름은 최대 255자까지 입력 가능합니다.")
    @Schema(description = "배우 이름", example = "송강호", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "생년월일 (yyyy-MM-dd)", example = "1967-01-17", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate birthDate;

    @Schema(description = "배우 소개", example = "배우입니다.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String biography;
}