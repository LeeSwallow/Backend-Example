package com.pnu.geoscopetest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name = "pin")
@Getter
@Setter
@RequiredArgsConstructor
public class Pin {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sigungu_id", nullable = false)
    private Sigungu sigungu;

    public Pin(String name, Point location, Sigungu sigungu) {
        this.name = name;
        this.location = location;
        this.sigungu = sigungu;
    }
}
