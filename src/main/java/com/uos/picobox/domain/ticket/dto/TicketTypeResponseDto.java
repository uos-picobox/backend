package com.uos.picobox.domain.ticket.dto;

import com.uos.picobox.domain.ticket.entity.TicketType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class TicketTypeResponseDto {

    @Schema(description = "티켓 종류 ID", example = "1")
    private Long ticketTypeId;

    @Schema(description = "티켓 종류 이름", example = "성인")
    private String typeName;

    @Schema(description = "티켓 종류 설명", example = "만 19세 이상 일반 관람객")
    private String description;

    public TicketTypeResponseDto(TicketType ticketType) {
        this.ticketTypeId = ticketType.getId();
        this.typeName = ticketType.getTypeName();
        this.description = ticketType.getDescription();
    }
}