package com.uos.picobox.domain.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RowDefinitionDto {

    @NotBlank(message = "행 식별자는 필수입니다.")
    @Size(max = 1, message = "행 식별자는 알파벳 한 글자입니다.")
    @Schema(description = "행 식별자 (예: A, B)", example = "A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String rowIdentifier;

    @NotNull(message = "행 좌석 수는 필수입니다.")
    @Min(value = 1, message = "행 좌석 수는 최소 1개 이상이어야 합니다.")
    @Schema(description = "해당 행의 좌석 수", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer numberOfSeats;
}