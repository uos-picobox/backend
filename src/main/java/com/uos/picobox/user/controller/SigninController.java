package com.uos.picobox.user.controller;

import com.uos.picobox.user.dto.SigninRequestDto;
import com.uos.picobox.user.dto.SigninResponseDto;
import com.uos.picobox.user.service.SigninService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원", description = "회원 정보를 처리합니다.")
@RestController
@RequestMapping("/api/user/signin")
@RequiredArgsConstructor
public class SigninController {
    private final SigninService signinService;

    @Operation(summary = "회원 로그인", description = "회원 로그인을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 로그인을 성공했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다(예: 올바르지 못한 loginId or Password)."),
            @ApiResponse(responseCode = "500", description = "서버 오류입니다.")
    })
    @PostMapping("")
    public ResponseEntity<?> signin(
            @Valid @RequestBody SigninRequestDto signinRequestDto) {
        SigninResponseDto response = signinService.signinCustomer(signinRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
