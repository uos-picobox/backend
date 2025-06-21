package com.uos.picobox.user.controller;

import com.uos.picobox.user.dto.SignoutResponseDto;
import com.uos.picobox.user.service.SignoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원", description = "회원 정보를 처리합니다.")
@RestController
@RequestMapping("/api/user/signout")
@RequiredArgsConstructor
public class SignoutController {
    private final SignoutService signoutService;

    @Operation(summary = "회원 로그아웃", description = "회원 로그아웃을 처리합니다.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 로그아웃을 성공했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 올바르지 못한 sessionId or 만료된 sessionId)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("")
    public ResponseEntity<?> signout(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String sessionId
    ) {
        SignoutResponseDto response = signoutService.signoutCustomer(sessionId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
