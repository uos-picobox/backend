package com.uos.picobox.client.controller.point;

import com.uos.picobox.domain.point.dto.MyPointHistoryResponseDto;
import com.uos.picobox.domain.point.service.PointHistoryService;
import com.uos.picobox.global.utils.SessionUtils;
import com.uos.picobox.user.dto.MyPointsResponseDto;
import com.uos.picobox.user.service.MyPointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "06. 포인트 내역 조회", description = "포인트 내역을 조회합니다.")
@RestController
@RequestMapping("/api/protected/get")
@RequiredArgsConstructor
public class PointController {
    private final MyPointsService myPointsService;
    private final PointHistoryService pointHistoryService;
    private final SessionUtils sessionUtils;

    @Operation(summary = "현재 포인트 잔액 조회", description = "현재 포인트 잔액을 조회합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "현재 포인트 잔액을 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 올바르지 못한 session)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @GetMapping("/point")
    public ResponseEntity<?> getMyPoints(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String sessionId,
            Authentication authentication
    ) {
        Long id = sessionUtils.findCustomerIdByAuthentication(authentication);
        MyPointsResponseDto response = myPointsService.findPointsByCustomerId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포인트 내역 조회", description = "포인트 내역을 조회합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포인트 내역을 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 올바르지 못한 session)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @GetMapping("/point-history")
    public ResponseEntity<?> getMyPointsHistory(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String sessionId,
            Authentication authentication
    ) {
        Long id = sessionUtils.findCustomerIdByAuthentication(authentication);
        MyPointHistoryResponseDto response = pointHistoryService.findPointHistory(id);
        return ResponseEntity.ok(response);
    }
}
