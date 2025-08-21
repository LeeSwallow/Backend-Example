package com.pnu.geoscopetest.repository;

import com.pnu.geoscopetest.entity.Pin;
import com.pnu.geoscopetest.entity.Sigungu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PinRepository extends JpaRepository<Pin, UUID> {
    List<Pin> findBySigungu(Sigungu sigungu);

    @Query(value = """
        SELECT * FROM pin p
        WHERE ST_Distance(
            p.location::geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude)::geography, 4326)
        ) < :distanceKm * 1000
    """, nativeQuery = true)
    List<Pin> findByDistanceKm(@Param("longitude") double longitude, @Param("latitude") double latitude, @Param("distanceKm") double distanceKm);
}
