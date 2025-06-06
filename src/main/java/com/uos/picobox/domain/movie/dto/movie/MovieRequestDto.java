package com.uos.picobox.domain.movie.dto.movie;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MovieRequestDto {

    @NotBlank(message = "영화 제목은 필수 입력 항목입니다.")
    @Size(max = 100, message = "영화 제목은 최대 100자까지 입력 가능합니다.")
    @Schema(description = "영화 제목", example = "극한직업", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Size(max = 500, message = "영화 설명은 최대 500자까지 입력 가능합니다.")
    @Schema(description = "영화 설명", example = "낮에는 치킨장사, 밤에는 잠복근무...", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @NotNull(message = "상영 시간(분)은 필수 입력 항목입니다.")
    @Positive(message = "상영 시간은 양수여야 합니다.")
    @Max(value = 9999, message = "상영 시간은 9999분을 초과할 수 없습니다.")
    @Schema(description = "상영 시간 (분 단위)", example = "111", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duration;

    @NotNull(message = "개봉일은 필수 입력 항목입니다.")
    @Schema(description = "개봉일 (yyyy-MM-dd)", example = "2019-01-23", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate releaseDate;

    @Schema(description = "상영 종료 예정일 (yyyy-MM-dd 형식, 선택 사항). 지정하지 않으면 미정으로 간주.",
            example = "2025-08-31", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate screeningEndDate;

    @Size(max = 30, message = "언어는 최대 30자까지 입력 가능합니다.")
    @Schema(description = "언어", example = "한국어", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String language;

    @Size(max = 50, message = "감독명은 최대 50자까지 입력 가능합니다.")
    @Schema(description = "감독", example = "이병헌", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String director;

    @NotNull(message = "배급사 ID는 필수 입력 항목입니다.")
    @Schema(description = "배급사 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long distributorId;

    @NotNull(message = "영화 등급 ID는 필수 입력 항목입니다.")
    @Schema(description = "영화 등급 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long movieRatingId;

    @Schema(description = "영화 장르 ID 목록", example = "[1, 2]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<Long> genreIds;

    @Schema(description = "출연진 정보 목록", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<MovieCastRequestDto> movieCasts;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MovieCastRequestDto {
        @NotNull(message = "배우 ID는 필수입니다.")
        @Schema(description = "배우 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long actorId;

        @NotBlank(message = "역할명은 필수입니다.")
        @Size(max = 50, message = "역할명은 최대 50자까지 입니다.")
        @Schema(description = "영화 내 역할", example = "고반장", requiredMode = Schema.RequiredMode.REQUIRED)
        private String role;
    }
}