package com.pnu.springsecuritytest.entity;

import com.pnu.springsecuritytest.common.model.TokenInfo;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@RequiredArgsConstructor
public class RefreshToken extends BaseEntity {

    private String token;

    @Column(name = "expired_at", nullable = false)
    LocalDateTime expiredAt;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    public RefreshToken(TokenInfo tokenInfo, User user) {
        this.token = tokenInfo.value();
        this.expiredAt = tokenInfo.expiration();
        this.user = user;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }
}
