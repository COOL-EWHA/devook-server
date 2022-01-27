package com.ewha.devookserver.domain.auth;

import com.ewha.devookserver.domain.user.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponse {
    private String nickname;
    private String email;
    private String accessToken;
    private String refreshToken;
    private boolean isExistUser;


    @Builder
    public LoginResponse(Long id, String nickname, String email, String imageUrl, Role role, String tokenType, String accessToken, String refreshToken, boolean isExistUser) {
        this.nickname = nickname;
        this.email = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.isExistUser = isExistUser;
    }
}
