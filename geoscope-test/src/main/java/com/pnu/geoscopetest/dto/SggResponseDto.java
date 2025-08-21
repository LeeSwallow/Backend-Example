package com.pnu.geoscopetest.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pnu.geoscopetest.entity.Sigungu;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SggResponseDto(
    Long id,
    String sidoCode,
    String sggCode,
    String sidoName,
    String sggName
) {

    public static SggResponseDto fromEntity(Sigungu entity) {
        return new SggResponseDto(
            entity.getId(),
            entity.getSidoCode(),
            entity.getSggCode(),
            entity.getSidoName(),
            entity.getSggName()
        );
    }


}
