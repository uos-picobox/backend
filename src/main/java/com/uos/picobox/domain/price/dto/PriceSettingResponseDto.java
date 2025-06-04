package com.uos.picobox.domain.price.dto;

import com.uos.picobox.domain.price.entity.RoomTicketTypePrice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class PriceSettingResponseDto {

    @Schema(description = "상영관 ID", example = "1")
    private Long roomId;

    @Schema(description = "상영관 이름", example = "1관 IMAX")
    private String roomName;

    @Schema(description = "티켓 종류 ID", example = "1")
    private Long ticketTypeId;

    @Schema(description = "티켓 종류 이름", example = "성인")
    private String ticketTypeName;

    @Schema(description = "설정된 가격", example = "15000")
    private Integer price;

    public PriceSettingResponseDto(RoomTicketTypePrice priceSetting) {
        this.roomId = priceSetting.getScreeningRoom().getId();
        this.roomName = priceSetting.getScreeningRoom().getRoomName();
        this.ticketTypeId = priceSetting.getTicketType().getId();
        this.ticketTypeName = priceSetting.getTicketType().getTypeName();
        this.price = priceSetting.getPrice();
    }
}