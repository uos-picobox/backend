package com.uos.picobox.domain.movie.dto.genre;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MovieGenreRequestDto {

    @NotBlank(message = "장르명은 필수 입력 항목입니다.")
    @Size(max = 30, message = "장르명은 최대 30자까지 입력 가능합니다.")
    @Schema(description = "영화 장르명", example = "드라마", requiredMode = Schema.RequiredMode.REQUIRED)
    private String genreName;
}