package com.pnu.chattest.dto.user;

import java.util.UUID;

public record CreateUserDto(
    String nickname,
    UUID chatRoomId
) {
}
