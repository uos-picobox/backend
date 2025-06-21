package com.uos.picobox.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReservationRequestDto {

    @NotNull(message = "상영 ID는 필수입니다.")
    @Schema(description = "예매할 상영 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long screeningId;

    @NotEmpty(message = "티켓 유형별 인원수 정보는 최소 하나 이상 있어야 합니다.")
    @Schema(description = "티켓 유형별 인원수 목록")
    private List<TicketTypeInfo> ticketTypes;

    @NotEmpty(message = "선택한 좌석 목록은 최소 하나 이상 있어야 합니다.")
    @Schema(description = "선택한 좌석 ID 목록")
    private List<Long> seatIds;

    @NotNull(message = "사용할 포인트는 필수입니다. (사용 안 할 시 0)")
    @Min(value = 0, message = "사용 포인트는 0 이상이어야 합니다.")
    @Schema(description = "사용할 포인트 금액", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer usedPoints;

    // 티켓 유형별 인원수 정보
    @Getter
    @Setter
    @NoArgsConstructor
    public static class TicketTypeInfo {
        @NotNull
        @Schema(description = "티켓 종류 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long ticketTypeId;

        @NotNull
        @Min(value = 1, message = "인원수는 1명 이상이어야 합니다.")
        @Schema(description = "해당 티켓 종류의 인원수", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer count;
    }
}