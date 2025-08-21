package com.pnu.geoscopetest.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreatePinRequestDto(
        String name,
        double latitude,
        double longitude
) {

}
