package com.uos.picobox.domain.screening.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ScreeningRequestDto {

    @NotNull(message = "영화 ID는 필수입니다.")
    @Schema(description = "상영할 영화의 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long movieId;

    @NotNull(message = "상영관 ID는 필수입니다.")
    @Schema(description = "상영할 상영관의 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roomId;

    @NotNull(message = "상영 시작 시간은 필수입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    @Schema(description = "상영 시작 시간 (yyyy-MM-dd HH:mm 형식)", type = "string", example = "2025-06-02 10:30", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime screeningTime;
}