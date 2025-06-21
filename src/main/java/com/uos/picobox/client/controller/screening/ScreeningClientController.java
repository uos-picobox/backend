package com.uos.picobox.client.controller.screening;

import com.uos.picobox.domain.screening.dto.ScreeningScheduleResponseDto;
import com.uos.picobox.domain.screening.dto.ScreeningSeatsResponseDto;
import com.uos.picobox.domain.screening.service.ScreeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "사용자 - 상영 스케줄 조회", description = "사용자 대상 상영 스케줄 조회 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScreeningClientController {

    private final ScreeningService screeningService;

    @Operation(summary = "특정 날짜의 전체 상영 시간표 조회",
            description = "주어진 날짜의 모든 상영 스케줄 목록을 조회합니다. 결과는 상영 시간 오름차순으로 정렬됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영 시간표 조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 날짜에 상영 정보 없음")
    })
    @GetMapping("/screenings")
    public ResponseEntity<List<ScreeningScheduleResponseDto>> getScreeningsByDate(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식)", required = true, example = "2025-06-04")
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        List<ScreeningScheduleResponseDto> schedules = screeningService.getScreeningSchedulesByDate(date);
        if (schedules.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(schedules);
    }

    @Operation(summary = "특정 영화의 특정 날짜 상영 시간표 조회",
            description = "주어진 영화의 주어진 날짜 상영 스케줄 목록을 조회합니다. 결과는 상영 시간 오름차순으로 정렬됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상영 시간표 조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 조건에 맞는 상영 정보 없음"),
            @ApiResponse(responseCode = "404", description = "요청한 영화를 찾을 수 없습니다.")
    })
    @GetMapping("/movies/{movieId}/screenings")
    public ResponseEntity<List<ScreeningScheduleResponseDto>> getScreeningsByMovieAndDate(
            @Parameter(description = "영화를 식별하기 위한 ID", required = true)
            @PathVariable Long movieId,
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식)", required = true, example = "2025-06-04")
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        List<ScreeningScheduleResponseDto> schedules = screeningService.getScreeningSchedulesByMovieAndDate(movieId, date);
        if (schedules.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(schedules);
    }

    @Operation(summary = "특정 상영의 좌석 상태 전체 조회",
            description = "주어진 상영 ID에 해당하는 상영관의 모든 좌석 상태(예매 가능, 판매 완료 등) 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좌석 상태 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 상영 정보를 찾을 수 없습니다.")
    })
    @GetMapping("/screenings/{screeningId}/seats")
    public ResponseEntity<ScreeningSeatsResponseDto> getScreeningSeats(
            @Parameter(description = "좌석을 조회할 상영의 ID", required = true, example = "1")
            @PathVariable Long screeningId) {
        ScreeningSeatsResponseDto seatsResponse = screeningService.getSeatsForScreening(screeningId);
        return ResponseEntity.ok(seatsResponse);
    }
}