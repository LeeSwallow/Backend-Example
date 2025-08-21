package com.pnu.geoscopetest.repository;


import com.pnu.geoscopetest.entity.Sigungu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SggRepository extends JpaRepository<Sigungu, Long> {
    @Query("""
        SELECT s FROM Sigungu s
        WHERE ST_Contains(
                s.area, ST_SetSRID(ST_MakePoint(:long, :lat), 4326)
        )
    """)
    Optional<Sigungu> findByLocation(@Param("lat") double latitude, @Param("long") double longitude);
}
