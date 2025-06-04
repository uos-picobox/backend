package com.uos.picobox.admin.controller.room;

import com.uos.picobox.domain.room.dto.ScreeningRoomRequestDto;
import com.uos.picobox.domain.room.dto.ScreeningRoomResponseDto;
import com.uos.picobox.domain.room.service.ScreeningRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자 - 06. 상영관 관리", description = "상영관 및 좌석 정보 CRUD API (관리자용)")
@RestController
@RequestMapping("/api/admin/screening-rooms")
@RequiredArgsConstructor
public class AdminScreeningRoomController {

    private final ScreeningRoomService screeningRoomService;

    @Operation(summary = "상영관 등록", description = "새로운 상영관을 등록하고 좌석을 자동 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "상영관이 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 중복된 상영관명).")
    })
    @PostMapping(value = "/create")
    public ResponseEntity<ScreeningRoomResponseDto> createScreeningRoom(
            @Valid @RequestBody ScreeningRoomRequestDto requestDto) {
        ScreeningRoomResponseDto responseDto = screeningRoomService.registerScreeningRoom(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "상영관 전체 목록 조회", description = "등록된 모든 상영관 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영관 목록이 성공적으로 조회되었습니다.")
    })
    @GetMapping("/get")
    public ResponseEntity<List<ScreeningRoomResponseDto>> getAllScreeningRooms() {
        List<ScreeningRoomResponseDto> screeningRooms = screeningRoomService.findAllScreeningRooms();
        return ResponseEntity.ok(screeningRooms);
    }

    @Operation(summary = "특정 상영관 정보 조회", description = "ID로 특정 상영관의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영관 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 상영관을 찾을 수 없습니다.")
    })
    @GetMapping("/get/{roomId}")
    public ResponseEntity<ScreeningRoomResponseDto> getScreeningRoomById(
            @Parameter(description = "조회할 상영관 ID", required = true, example = "1")
            @PathVariable Long roomId) {
        ScreeningRoomResponseDto screeningRoom = screeningRoomService.findScreeningRoomById(roomId);
        return ResponseEntity.ok(screeningRoom);
    }

    @Operation(summary = "상영관 정보 수정", description = "기존 상영관 정보를 수정합니다. 좌석 배치 변경 시, 해당 상영관에 상영 스케줄이 없어야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영관 정보가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패, 사용 중인 상영관의 배치 변경 시도)."),
            @ApiResponse(responseCode = "404", description = "해당 상영관을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "상태 충돌입니다 (예: 상영 스케줄이 있는 상영관의 좌석 배치 변경 시도).")
    })
    @PutMapping(value = "/update/{roomId}")
    public ResponseEntity<ScreeningRoomResponseDto> updateScreeningRoom(
            @Parameter(description = "수정할 상영관 ID", required = true, example = "1")
            @PathVariable Long roomId,
            @Valid @RequestBody ScreeningRoomRequestDto requestDto) {
        ScreeningRoomResponseDto updatedScreeningRoom = screeningRoomService.editScreeningRoom(roomId, requestDto);
        return ResponseEntity.ok(updatedScreeningRoom);
    }

    @Operation(summary = "상영관 삭제", description = "상영관 정보를 삭제합니다. 연관된 모든 좌석도 함께 삭제됩니다. 해당 상영관에 상영 스케줄이 없어야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "상영관 정보가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 상영관을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "409", description = "상태 충돌입니다 (예: 상영 스케줄이 있는 상영관 삭제 시도).")
    })
    @DeleteMapping("/delete/{roomId}")
    public ResponseEntity<Void> deleteScreeningRoom(
            @Parameter(description = "삭제할 상영관 ID", required = true, example = "1")
            @PathVariable Long roomId) {
        screeningRoomService.removeScreeningRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}