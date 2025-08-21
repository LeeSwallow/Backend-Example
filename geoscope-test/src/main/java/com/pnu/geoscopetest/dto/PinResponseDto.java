package com.pnu.geoscopetest.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pnu.geoscopetest.entity.Pin;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PinResponseDto(
        UUID id,
        String name,
        double latitude,
        double longitude,
        Long sigunguId
) {
    public static PinResponseDto fromEntity(Pin entity) {
        return new PinResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getLocation().getY(),
                entity.getLocation().getX(),
                entity.getSigungu().getId()
        );
    }
}
