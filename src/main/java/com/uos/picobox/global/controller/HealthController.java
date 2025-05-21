package com.uos.picobox.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "00. 헬스 체크", description = "애플리케이션의 상태를 확인하는 API")
@RestController
public class HealthController {

    @Operation(
            summary = "애플리케이션 헬스 체크",
            description = "애플리케이션의 현재 동작 상태 및 간단한 정보를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "애플리케이션이 성공적으로 응답했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류입니다.")
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "PicoBox API Server is running!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}