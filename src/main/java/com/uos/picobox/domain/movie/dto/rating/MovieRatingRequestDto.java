package com.uos.picobox.domain.movie.dto.rating;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MovieRatingRequestDto {

    @NotBlank(message = "영화 등급명은 필수 입력 항목입니다.")
    @Size(max = 30, message = "영화 등급명은 최대 30자까지 입력 가능합니다.")
    @Schema(description = "영화 등급명", example = "전체 관람가", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ratingName;

    @Size(max = 500, message = "설명은 최대 500자까지 입력 가능합니다.")
    @Schema(description = "등급 설명", example = "모든 연령의 관람객이 관람할 수 있는 등급입니다.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;
}