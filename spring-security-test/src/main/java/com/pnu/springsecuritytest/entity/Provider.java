package com.pnu.springsecuritytest.entity;

import com.pnu.springsecuritytest.entity.type.ProviderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "providers")
@Getter
@Setter
@RequiredArgsConstructor
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProviderType name;
}
