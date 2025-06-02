package com.uos.picobox.admin.controller.price;

import com.uos.picobox.domain.price.dto.PriceSettingRequestDto;
import com.uos.picobox.domain.price.dto.PriceSettingResponseDto;
import com.uos.picobox.domain.price.service.PriceSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자 - 09. 상영관-티켓별 가격 정책 관리", description = "상영관 및 티켓 종류에 따른 가격 설정 CRUD API")
@RestController
@RequestMapping("/api/admin/price-settings")
@RequiredArgsConstructor
public class AdminPriceSettingController {

    private final PriceSettingService priceSettingService;

    @Operation(summary = "가격 설정/수정", description = "특정 상영관의 특정 티켓 종류에 대한 가격을 설정하거나 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "가격이 성공적으로 설정/수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패)."),
            @ApiResponse(responseCode = "404", description = "해당 상영관 또는 티켓 종류를 찾을 수 없습니다.")
    })
    @PostMapping("/set")
    public ResponseEntity<PriceSettingResponseDto> setOrUpdatePrice(
            @Valid @RequestBody PriceSettingRequestDto requestDto) {
        PriceSettingResponseDto responseDto = priceSettingService.setOrUpdatePrice(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "특정 가격 정책 조회", description = "특정 상영관의 특정 티켓 종류에 대한 가격을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "가격 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 가격 설정을 찾을 수 없습니다.")
    })
    @GetMapping("/get")
    public ResponseEntity<PriceSettingResponseDto> getPrice(
            @Parameter(description = "상영관 ID", required = true) @RequestParam Long roomId,
            @Parameter(description = "티켓 종류 ID", required = true) @RequestParam Long ticketTypeId) {
        PriceSettingResponseDto responseDto = priceSettingService.getPrice(roomId, ticketTypeId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "특정 상영관의 모든 가격 정책 조회", description = "특정 상영관에 설정된 모든 티켓 종류별 가격을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "가격 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 상영관을 찾을 수 없습니다.")
    })
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<PriceSettingResponseDto>> getPricesForRoom(
            @Parameter(description = "조회할 상영관 ID", required = true) @PathVariable Long roomId) {
        List<PriceSettingResponseDto> responseDtos = priceSettingService.getPricesForRoom(roomId);
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(summary = "가격 정책 삭제", description = "특정 상영관의 특정 티켓 종류에 대한 가격 설정을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "가격 설정이 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "삭제할 가격 설정을 찾을 수 없습니다.")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<Void> removePrice(
            @Parameter(description = "상영관 ID", required = true) @RequestParam Long roomId,
            @Parameter(description = "티켓 종류 ID", required = true) @RequestParam Long ticketTypeId) {
        priceSettingService.removePrice(roomId, ticketTypeId);
        return ResponseEntity.noContent().build();
    }
}