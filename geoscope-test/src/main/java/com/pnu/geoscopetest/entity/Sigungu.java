package com.pnu.geoscopetest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.geolatte.geom.MultiPolygon;

@Entity
@Table(name = "sigungu")
@Getter
@Setter
@RequiredArgsConstructor
public class Sigungu {
    @Id
    private Long id;

    @Column(name="sido")
    private String sidoCode;
    @Column(name="sgg")
    private String sggCode;

    @Column(name="sidonm")
    private String sidoName;
    @Column(name = "sggnm")
    private String sggName;

    @Column(name = "geom", columnDefinition = "geometry(MultiPolygon, 4326)")
    private MultiPolygon area;
}
