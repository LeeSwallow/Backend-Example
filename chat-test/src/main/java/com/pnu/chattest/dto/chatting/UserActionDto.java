package com.pnu.chattest.dto.chatting;

import com.pnu.chattest.dto.user.UserInfoDto;

public record UserActionDto(
        UserInfoDto userInfo,
        Action action
) {

}
