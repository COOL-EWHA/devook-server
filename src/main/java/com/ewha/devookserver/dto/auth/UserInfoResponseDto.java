package com.ewha.devookserver.dto.auth;

import lombok.*;

@Getter
public class UserInfoResponseDto {
    private final String email;
    private final String nickname;

    @Builder
    public UserInfoResponseDto(String email, String nickname){
        this.email=email;
        this.nickname=nickname;
    }

}
