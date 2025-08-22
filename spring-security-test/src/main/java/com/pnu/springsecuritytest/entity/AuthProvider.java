package com.pnu.springsecuritytest.entity;

import com.pnu.springsecuritytest.entity.type.AuthProviderId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_auth_provider")
@Getter
@Setter
@RequiredArgsConstructor
public class AuthProvider {
    @EmbeddedId
    private AuthProviderId id;

    @MapsId("providerId")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "client_id", nullable = false)
    private String clientId;
}
