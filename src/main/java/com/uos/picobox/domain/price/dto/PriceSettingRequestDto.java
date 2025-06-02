package com.uos.picobox.domain.price.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PriceSettingRequestDto {

    @NotNull(message = "상영관 ID는 필수입니다.")
    @Schema(description = "가격을 설정할 상영관 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roomId;

    @NotNull(message = "티켓 종류 ID는 필수입니다.")
    @Schema(description = "가격을 설정할 티켓 종류 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ticketTypeId;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    @Schema(description = "설정할 가격", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer price;
}