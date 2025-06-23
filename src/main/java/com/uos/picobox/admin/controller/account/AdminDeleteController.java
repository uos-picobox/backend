package com.uos.picobox.admin.controller.account;

import com.uos.picobox.admin.service.AdminDeleteService;
import com.uos.picobox.global.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 - 00. 관리자 탈퇴", description = "관리자 탈퇴를 수행합니다.")
@RestController
@RequestMapping("/api/admin/delete")
@RequiredArgsConstructor
public class AdminDeleteController {
    private final AdminDeleteService adminDeleteService;
    private final SessionUtils sessionUtils;

    @Operation(summary = "관리자 탈퇴", description = "관리자 탈퇴를 진행합니다. 이 API 호출 이전에 안내메세지를 안내해주세요.", security = @SecurityRequirement(name = "sessionAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 탈퇴가 성공적으로 수행되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패)."),
            @ApiResponse(responseCode = "500", description = "서버 에러입니다. 관리자에게 문의하세요")
    })
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAdmin(
            @Parameter(hidden = true) @RequestHeader("Authorization") String sessionId,
            Authentication authentication) {
        Long adminId = sessionUtils.findAdminIdByAuthentication(authentication);
        adminDeleteService.deleteAdminById(adminId);
        return ResponseEntity.noContent().build();
    }
}
