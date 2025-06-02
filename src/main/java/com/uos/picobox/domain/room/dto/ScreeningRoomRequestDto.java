package com.uos.picobox.domain.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ScreeningRoomRequestDto {

    @NotBlank(message = "상영관 이름은 필수 입력 항목입니다.")
    @Size(max = 50, message = "상영관 이름은 최대 50자까지 입력 가능합니다.")
    @Schema(description = "상영관 이름", example = "1관 (IMAX)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roomName;

    @Valid
    @Schema(description = "좌석 배치 정의 목록 (행별 좌석 수). 상영관 생성 시 필수, 수정 시 좌석 배치를 변경할 경우에만 포함.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<RowDefinitionDto> rowDefinitions;
}