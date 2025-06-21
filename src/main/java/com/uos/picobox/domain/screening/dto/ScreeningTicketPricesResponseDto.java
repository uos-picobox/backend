package com.uos.picobox.domain.screening.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ScreeningTicketPricesResponseDto {

    @Schema(description = "상영 ID", example = "1")
    private Long screeningId;

    @Schema(description = "상영관 ID", example = "1")
    private Long roomId;

    @Schema(description = "티켓 유형별 가격 목록")
    private List<TicketPriceInfo> ticketPrices;

    public ScreeningTicketPricesResponseDto(Long screeningId, Long roomId, List<TicketPriceInfo> ticketPrices) {
        this.screeningId = screeningId;
        this.roomId = roomId;
        this.ticketPrices = ticketPrices;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TicketPriceInfo {
        @Schema(description = "티켓 유형 ID", example = "1")
        private Long ticketTypeId;

        @Schema(description = "티켓 유형명", example = "성인")
        private String typeName;

        @Schema(description = "가격", example = "14000")
        private Integer price;

        public TicketPriceInfo(Long ticketTypeId, String typeName, Integer price) {
            this.ticketTypeId = ticketTypeId;
            this.typeName = typeName;
            this.price = price;
        }
    }
} 