package com.pnu.springsecuritytest.common.model;

import java.time.LocalDateTime;

public record TokenInfo(
        String value,
        LocalDateTime expiration
) {
}
