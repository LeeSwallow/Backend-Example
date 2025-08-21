package com.pnu.geoscopetest.controller;

import com.pnu.geoscopetest.dto.SggResponseDto;
import com.pnu.geoscopetest.service.SggService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SggController {
    private final SggService sggService;

    @GetMapping("/api/sgg")
    public ResponseEntity<SggResponseDto> getSggByCoordinates(
            @RequestParam(value = "lat", required = true) double latitude,
            @RequestParam(value = "long", required = true) double longitude
    ) {
        var sgg = sggService.findByCoordinates(latitude, longitude);
        return ResponseEntity.ok(SggResponseDto.fromEntity(sgg));
    }
}
