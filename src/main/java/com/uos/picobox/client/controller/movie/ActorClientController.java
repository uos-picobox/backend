package com.uos.picobox.client.controller.movie;

import com.uos.picobox.domain.movie.dto.actor.ActorResponseDto;
import com.uos.picobox.domain.movie.service.ActorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "03. 사용자 - 배우 정보 조회", description = "사용자자용 배우 관련 API")
@RestController
@RequestMapping("/api/actors")
@RequiredArgsConstructor
public class ActorClientController {

    private final ActorService actorService;

    @Operation(summary = "배우 상세 정보 조회", description = "ID로 특정 배우의 상세 정보와 필모그래피를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배우 정보가 성공적으로 조회되었습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ActorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 배우를 찾을 수 없습니다.")
    })
    @GetMapping("/get/{actorId}")
    public ResponseEntity<ActorResponseDto> getActorById(
            @Parameter(description = "조회할 배우 ID", required = true) @PathVariable Long actorId) {
        ActorResponseDto actor = actorService.findActorByIdWithFilmography(actorId);
        return ResponseEntity.ok(actor);
    }
} 