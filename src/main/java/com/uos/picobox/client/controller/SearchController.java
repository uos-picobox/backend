package com.uos.picobox.client.controller;

import com.uos.picobox.client.dto.SearchResponseDto;
import com.uos.picobox.client.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "검색 API", description = "영화와 배우 통합 검색 관련 API")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    @Operation(
        summary = "영화 및 배우 통합 검색",
        description = "입력된 키워드로 영화 제목과 배우 이름을 동시에 검색합니다.\n" +
                     "영화는 상영상태별(개봉예정>상영중>상영종료) 및 최신 개봉일순으로,\n" +
                     "배우는 이름순으로 정렬됩니다."
    )
    public ResponseEntity<SearchResponseDto> search(
            @Parameter(description = "검색 키워드", example = "이")
            @RequestParam(name = "query") String keyword
    ) {
        SearchResponseDto result = searchService.search(keyword);
        return ResponseEntity.ok(result);
    }
} 