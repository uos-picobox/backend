package com.uos.picobox.admin.controller.screening;

import com.uos.picobox.domain.screening.dto.ScreeningRequestDto;
import com.uos.picobox.domain.screening.dto.ScreeningResponseDto;
import com.uos.picobox.domain.screening.service.ScreeningService;
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

@Tag(name = "관리자 - 07. 상영 스케줄 관리", description = "영화 상영 스케줄 CRUD API (관리자용)")
@RestController
@RequestMapping("/api/admin/screenings")
@RequiredArgsConstructor
public class AdminScreeningController {

    private final ScreeningService screeningService;

    @Operation(summary = "상영 스케줄 등록", description = "새로운 상영 스케줄을 등록합니다. 회차 및 상영일은 자동 계산됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "상영 스케줄이 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 시간 겹침, 유효하지 않은 ID)."),
            @ApiResponse(responseCode = "404", description = "관련 영화 또는 상영관을 찾을 수 없습니다.")
    })
    @PostMapping("/create")
    public ResponseEntity<ScreeningResponseDto> createScreening(
            @Valid @RequestBody ScreeningRequestDto requestDto) {
        ScreeningResponseDto responseDto = screeningService.registerScreening(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "상영 스케줄 전체 목록 조회", description = "모든 상영 스케줄 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영 스케줄 목록이 성공적으로 조회되었습니다.")
    })
    @GetMapping("/get")
    public ResponseEntity<List<ScreeningResponseDto>> getAllScreenings() {
        List<ScreeningResponseDto> screenings = screeningService.findAllScreenings();
        return ResponseEntity.ok(screenings);
    }

    @Operation(summary = "특정 상영 스케줄 조회", description = "ID로 특정 상영 스케줄 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영 스케줄 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 상영 스케줄을 찾을 수 없습니다.")
    })
    @GetMapping("/get/{screeningId}")
    public ResponseEntity<ScreeningResponseDto> getScreeningById(
            @Parameter(description = "조회할 상영 스케줄 ID", required = true) @PathVariable Long screeningId) {
        ScreeningResponseDto screening = screeningService.findScreeningById(screeningId);
        return ResponseEntity.ok(screening);
    }

    @Operation(summary = "상영 스케줄 수정", description = "기존 상영 스케줄 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영 스케줄 정보가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 시간 겹침, 유효하지 않은 ID)."),
            @ApiResponse(responseCode = "404", description = "해당 상영 스케줄 또는 관련 정보를 찾을 수 없습니다.")
    })
    @PutMapping("/update/{screeningId}")
    public ResponseEntity<ScreeningResponseDto> updateScreening(
            @Parameter(description = "수정할 상영 스케줄 ID", required = true) @PathVariable Long screeningId,
            @Valid @RequestBody ScreeningRequestDto requestDto) {
        ScreeningResponseDto updatedScreening = screeningService.editScreening(screeningId, requestDto);
        return ResponseEntity.ok(updatedScreening);
    }

    @Operation(summary = "상영 스케줄 삭제", description = "상영 스케줄 정보를 삭제합니다. 연관된 상영 좌석 정보도 함께 삭제됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "상영 스케줄 정보가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 상영 스케줄을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 이미 예매가 진행된 스케줄).")
    })
    @DeleteMapping("/delete/{screeningId}")
    public ResponseEntity<Void> deleteScreening(
            @Parameter(description = "삭제할 상영 스케줄 ID", required = true) @PathVariable Long screeningId) {
        screeningService.removeScreening(screeningId);
        return ResponseEntity.noContent().build();
    }
}