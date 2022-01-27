package com.ewha.devookserver.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class LoginFinalResponseDto {
    private final String email;
    private final String nickname;
    private final String accessToken;
    private final String refreshToken;


    @Builder
    public LoginFinalResponseDto(String email, String nickname, String accessToken, String refreshToken){
        this.email=email;
        this.nickname=nickname;
        this.accessToken=accessToken;
        this.refreshToken=refreshToken;
    }

}
