package com.pnu.geoscopetest.service;

import com.pnu.geoscopetest.entity.Pin;
import com.pnu.geoscopetest.entity.Sigungu;
import com.pnu.geoscopetest.repository.PinRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PinService {

    private final PinRepository pinRepository;
    private final SggService sggService;
    private final GeometryFactory geometryFactory;

    public Pin save(String name, double latitude, double longitude) {
        Sigungu sgg = sggService.findByCoordinates(latitude, longitude);
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        return pinRepository.save(new Pin(name, location, sgg));
    }

    public List<Pin> findAll() {
        return pinRepository.findAll();
    }

    public Pin findById(UUID id) {
        return pinRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 위치의 핀을 찾을 수 없습니다. " + id));
    }

    public List<Pin> findWithInDistanceKm(double latitude, double longitude, double distanceKm) {
        return pinRepository.findByDistanceKm(latitude, longitude, distanceKm);
    }

    public List<Pin> findBySigunguId(Long sigunguId) {
        Sigungu sigungu = sggService.findById(sigunguId);
        return pinRepository.findBySigungu(sigungu);
    }

    public void deleteById(UUID id) {
        findById(id);
        pinRepository.deleteById(id);
    }
}
