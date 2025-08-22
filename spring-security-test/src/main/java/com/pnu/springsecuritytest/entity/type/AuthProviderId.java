package com.pnu.springsecuritytest.entity.type;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Data
public class AuthProviderId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "provider_id")
    private Long providerId;
}
