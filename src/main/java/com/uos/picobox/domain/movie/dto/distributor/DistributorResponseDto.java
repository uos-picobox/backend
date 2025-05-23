package com.uos.picobox.domain.movie.dto.distributor;

import com.uos.picobox.domain.movie.entity.Distributor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class DistributorResponseDto {

    @Schema(description = "배급사 ID", example = "1")
    private Long distributorId;

    @Schema(description = "배급사명", example = "CJ ENM")
    private String name;

    @Schema(description = "주소", example = "서울특별시 마포구 상암산로 66")
    private String address;

    @Schema(description = "전화번호", example = "02-1234-5678")
    private String phone;

    public DistributorResponseDto(Distributor distributor) {
        this.distributorId = distributor.getId();
        this.name = distributor.getName();
        this.address = distributor.getAddress();
        this.phone = distributor.getPhone();
    }
}