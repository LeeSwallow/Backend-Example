package com.pnu.geoscopetest.controller;

import com.pnu.geoscopetest.dto.CreatePinRequestDto;
import com.pnu.geoscopetest.dto.PinResponseDto;
import com.pnu.geoscopetest.service.PinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
public class PinController {
    private final PinService pinService;

    @PostMapping
    public ResponseEntity<PinResponseDto> addPin(
        @RequestBody CreatePinRequestDto dto
    ) {
        var pin = pinService.save(dto.name(), dto.latitude(), dto.longitude());
        return ResponseEntity.ok(PinResponseDto.fromEntity(pin));
    }

    @GetMapping
    public ResponseEntity<List<PinResponseDto>> getPins() {
        return ResponseEntity.ok(pinService.findAll()
                .stream()
                .map(PinResponseDto::fromEntity)
                .toList()
        );
    }

    @GetMapping("/distance")
    public ResponseEntity<List<PinResponseDto>> getPinsWithinDistance(
            @RequestParam(value = "lat", required = true) double latitude,
            @RequestParam(value = "long", required = true) double longitude,
            @RequestParam(value = "km", required = true) double distanceKm
    ) {
        return ResponseEntity.ok(pinService.findWithInDistanceKm(latitude, longitude, distanceKm)
                .stream()
                .map(PinResponseDto::fromEntity)
                .toList()
        );
    }

    @GetMapping("/sgg")
    public ResponseEntity<List<PinResponseDto>> getPinsBySigunguId(
            @RequestParam(value = "sggId", required = true) Long sigunguId
    ) {
        return ResponseEntity.ok(pinService.findBySigunguId(sigunguId)
                .stream()
                .map(PinResponseDto::fromEntity)
                .toList()
        );
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deletePin(
            @PathVariable("id") UUID id
    ) {
        pinService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
