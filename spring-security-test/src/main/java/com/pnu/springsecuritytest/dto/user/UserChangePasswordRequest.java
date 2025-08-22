package com.pnu.springsecuritytest.dto.user;

public record UserChangePasswordRequest(
        String oldPassword,
        String newPassword,
        String confirmNewPassword
) {
    
}
