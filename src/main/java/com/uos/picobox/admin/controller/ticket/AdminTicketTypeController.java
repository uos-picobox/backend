package com.uos.picobox.admin.controller.ticket;

import com.uos.picobox.domain.ticket.dto.TicketTypeRequestDto;
import com.uos.picobox.domain.ticket.dto.TicketTypeResponseDto;
import com.uos.picobox.domain.ticket.service.TicketTypeService;
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

@Tag(name = "관리자 - 08. 티켓 종류 관리", description = "티켓 종류(성인, 청소년 등) CRUD API")
@RestController
@RequestMapping("/api/admin/ticket-types")
@RequiredArgsConstructor
public class AdminTicketTypeController {

    private final TicketTypeService ticketTypeService;

    @Operation(summary = "티켓 종류 등록", description = "새로운 티켓 종류를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "티켓 종류가 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패)."),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 티켓 종류 이름입니다.")
    })
    @PostMapping("/create")
    public ResponseEntity<TicketTypeResponseDto> createTicketType(
            @Valid @RequestBody TicketTypeRequestDto requestDto) {
        TicketTypeResponseDto responseDto = ticketTypeService.registerTicketType(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "티켓 종류 전체 목록 조회", description = "모든 티켓 종류 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "티켓 종류 목록이 성공적으로 조회되었습니다.")
    })
    @GetMapping("/get")
    public ResponseEntity<List<TicketTypeResponseDto>> getAllTicketTypes() {
        List<TicketTypeResponseDto> ticketTypes = ticketTypeService.findAllTicketTypes();
        return ResponseEntity.ok(ticketTypes);
    }

    @Operation(summary = "특정 티켓 종류 조회", description = "ID로 특정 티켓 종류 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "티켓 종류 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 티켓 종류를 찾을 수 없습니다.")
    })
    @GetMapping("/get/{ticketTypeId}")
    public ResponseEntity<TicketTypeResponseDto> getTicketTypeById(
            @Parameter(description = "조회할 티켓 종류 ID", required = true) @PathVariable Long ticketTypeId) {
        TicketTypeResponseDto ticketType = ticketTypeService.findTicketTypeById(ticketTypeId);
        return ResponseEntity.ok(ticketType);
    }

    @Operation(summary = "티켓 종류 수정", description = "기존 티켓 종류 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "티켓 종류 정보가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다 (예: 유효성 검사 실패)."),
            @ApiResponse(responseCode = "404", description = "해당 티켓 종류를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 티켓 종류 이름입니다.")
    })
    @PutMapping("/update/{ticketTypeId}")
    public ResponseEntity<TicketTypeResponseDto> updateTicketType(
            @Parameter(description = "수정할 티켓 종류 ID", required = true) @PathVariable Long ticketTypeId,
            @Valid @RequestBody TicketTypeRequestDto requestDto) {
        TicketTypeResponseDto updatedTicketType = ticketTypeService.editTicketType(ticketTypeId, requestDto);
        return ResponseEntity.ok(updatedTicketType);
    }

    @Operation(summary = "티켓 종류 삭제", description = "티켓 종류 정보를 삭제합니다. 해당 종류가 가격 정책에서 사용 중이면 삭제할 수 없습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "티켓 종류 정보가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 티켓 종류를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "해당 티켓 종류가 가격 정책에서 사용 중이므로 삭제할 수 없습니다.")
    })
    @DeleteMapping("/delete/{ticketTypeId}")
    public ResponseEntity<Void> deleteTicketType(
            @Parameter(description = "삭제할 티켓 종류 ID", required = true) @PathVariable Long ticketTypeId) {
        ticketTypeService.removeTicketType(ticketTypeId);
        return ResponseEntity.noContent().build();
    }
}