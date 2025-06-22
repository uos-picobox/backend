package com.uos.picobox.client.controller.reservation;

import com.uos.picobox.domain.reservation.dto.*;
import com.uos.picobox.domain.reservation.service.ReservationService;
import com.uos.picobox.global.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "05. 회원/게스트 - 티켓 예매", description = "좌석 선택, 예매, 결제 완료 처리 API (회원/게스트 모두 이용 가능)")
@RestController
@RequestMapping("/api/protected/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final SessionUtils sessionUtils;

    @Operation(summary = "좌석 선점 (HOLD)", description = "사용자가 선택한 좌석을 10분간 선점합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좌석 선점 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상영 또는 좌석"),
            @ApiResponse(responseCode = "409", description = "이미 선택된 좌석")
    })
    @PostMapping("/hold")
    public ResponseEntity<Void> holdSeats(
            @Valid @RequestBody SeatRequestDto dto,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Map<String, Object> sessionInfo = sessionUtils.findSessionInfoByAuthentication(authentication);
        reservationService.holdSeats(dto, sessionInfo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "좌석 선점 해제 (Release)", description = "사용자가 명시적으로 취소하거나 이탈 시 호출하여 선점한 좌석을 해제합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좌석 선점 해제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상영 또는 좌석")
    })
    @PostMapping("/release")
    public ResponseEntity<Void> releaseSeats(
            @Valid @RequestBody SeatRequestDto dto,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Map<String, Object> sessionInfo = sessionUtils.findSessionInfoByAuthentication(authentication);
        reservationService.releaseSeats(dto, sessionInfo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "결제 전 예매 생성", description = "선점한 좌석과 사용할 포인트를 기반으로 결제 대기(PENDING) 상태의 예매를 생성합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "결제 대기 예매 생성 성공"),
            @ApiResponse(responseCode = "400", description = "포인트 부족 또는 잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "409", description = "선점되지 않은 좌석 포함")
    })
    @PostMapping("/create")
    public ResponseEntity<ReservationResponseDto> createPendingReservation(
            @Valid @RequestBody ReservationRequestDto dto,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Map<String, Object> sessionInfo = sessionUtils.findSessionInfoByAuthentication(authentication);
        ReservationResponseDto responseDto = reservationService.createPendingReservation(dto, sessionInfo);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "결제 완료 처리", description = "결제 성공 후 호출되어 예매 상태를 최종 완료(COMPLETED) 처리합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 완료 처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 예약"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 예약")
    })
    @PostMapping("/complete")
    public ResponseEntity<Void> completeReservation(
            @RequestParam
            @NotNull(message = "예약 ID는 필수입니다.")
            @Schema(description = "결제를 완료할 예약 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long reservationId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Map<String, Object> sessionInfo = sessionUtils.findSessionInfoByAuthentication(authentication);
        reservationService.completeReservation(reservationId, sessionInfo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 예매 내역 조회", description = "현재 로그인한 사용자의 예매 내역을 조회합니다. 과거/현재 구분되며 상영일 기준으로 정렬됩니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예매 내역 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationListResponseDto>> getMyReservations(
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Map<String, Object> sessionInfo = sessionUtils.findSessionInfoByAuthentication(authentication);
        List<ReservationListResponseDto> reservations = reservationService.getReservationList(sessionInfo);
        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "예매 상세 정보 조회", description = "특정 예매의 상세 정보를 조회합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예매 상세 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 예매")
    })
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationDetailResponseDto> getReservationDetail(
            @Parameter(description = "예매 ID", required = true) @PathVariable Long reservationId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Map<String, Object> sessionInfo = sessionUtils.findSessionInfoByAuthentication(authentication);
        ReservationDetailResponseDto detail = reservationService.getReservationDetail(reservationId, sessionInfo);
        return ResponseEntity.ok(detail);
    }

    @Operation(summary = "모바일 티켓 조회", description = "예매에 대한 모바일 티켓 정보를 조회합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "티켓 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 예매")
    })
    @GetMapping("/{reservationId}/ticket")
    public ResponseEntity<TicketResponseDto> getTicket(
            @Parameter(description = "예매 ID", required = true) @PathVariable Long reservationId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Map<String, Object> sessionInfo = sessionUtils.findSessionInfoByAuthentication(authentication);
        TicketResponseDto ticket = reservationService.getTicket(reservationId, sessionInfo);
        return ResponseEntity.ok(ticket);
    }
}