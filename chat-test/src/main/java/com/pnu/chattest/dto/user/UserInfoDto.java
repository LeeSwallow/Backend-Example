package com.pnu.chattest.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserInfoDto(
        UUID id,
        String nickname,
        LocalDateTime lastActiveTime
) {

}
