package com.uos.picobox.domain.movie.dto.distributor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DistributorRequestDto {

    @NotBlank(message = "배급사 이름은 필수 입력 항목입니다.")
    @Size(max = 50, message = "배급사 이름은 최대 50자까지 입력 가능합니다.")
    @Schema(description = "배급사명", example = "CJ ENM", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 300, message = "주소는 최대 300자까지 입력 가능합니다.")
    @Schema(description = "주소", example = "서울특별시 마포구 상암산로 66", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String address;

    @Size(max = 20, message = "전화번호는 최대 20자까지 입력 가능합니다.")
    @Schema(description = "전화번호", example = "02-1234-5678", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String phone;
}