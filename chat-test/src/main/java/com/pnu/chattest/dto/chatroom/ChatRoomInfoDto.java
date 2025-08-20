package com.pnu.chattest.dto.chatroom;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomInfoDto(
        UUID roomId,
        String name,
        String description,
        LocalDateTime createdAt
) {
}
