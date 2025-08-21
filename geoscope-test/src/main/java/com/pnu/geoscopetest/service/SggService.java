package com.pnu.geoscopetest.service;

import com.pnu.geoscopetest.entity.Sigungu;
import com.pnu.geoscopetest.repository.SggRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SggService {
    private final SggRepository sggRepository;

    public Sigungu findByCoordinates(double latitude, double longitude) {
        Optional<Sigungu> sgg = sggRepository.findByLocation(latitude, longitude);
        if (sgg.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "해당 위치에 해당하는 시군구를 찾을 수 없습니다."
            );
        }
        return sgg.get();
    }

    public Sigungu findById(Long id) {
        return sggRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 시군구를 찾을 수 없습니다. " + id));
    }
}
