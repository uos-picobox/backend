package com.uos.picobox.domain.room.dto;

import com.uos.picobox.domain.room.entity.ScreeningRoom;
import com.uos.picobox.domain.room.entity.Seat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public class ScreeningRoomResponseDto {

    @Schema(description = "상영관 ID", example = "1")
    private Long roomId;

    @Schema(description = "상영관 이름", example = "1관 (IMAX)")
    private String roomName;

    @Schema(description = "총 좌석 수", example = "120")
    private Integer capacity;

    @Schema(description = "행별 좌석 배치 정보 (예: A열 10석, B열 12석)")
    private List<RowDefinitionDto> seatLayout;

    public ScreeningRoomResponseDto(ScreeningRoom screeningRoom) {
        this.roomId = screeningRoom.getId();
        this.roomName = screeningRoom.getRoomName();
        this.capacity = screeningRoom.getCapacity();
        this.seatLayout = reconstructSeatLayout(screeningRoom.getSeats());
    }

    private List<RowDefinitionDto> reconstructSeatLayout(List<Seat> seats) {
        if (seats == null || seats.isEmpty()) {
            return Collections.emptyList();
        }

        Pattern rowPattern = Pattern.compile("^([A-Za-z])");

        Map<String, Long> seatsPerRow = seats.stream()
                .map(seat -> {
                    Matcher matcher = rowPattern.matcher(seat.getSeatNumber());
                    matcher.find();
                    return matcher.group(1).toUpperCase();
                })
                .collect(Collectors.groupingBy(
                        rowIdentifier -> rowIdentifier,
                        Collectors.counting()
                ));

        return seatsPerRow.entrySet().stream()
                .map(entry -> {
                    RowDefinitionDto rowDef = new RowDefinitionDto();
                    rowDef.setRowIdentifier(entry.getKey());
                    rowDef.setNumberOfSeats(entry.getValue().intValue());
                    return rowDef;
                })
                .sorted((rd1, rd2) -> rd1.getRowIdentifier().compareTo(rd2.getRowIdentifier()))
                .collect(Collectors.toList());
    }
}