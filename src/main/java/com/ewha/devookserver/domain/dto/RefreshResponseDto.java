package com.ewha.devookserver.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RefreshResponseDto {
    public String accessToken;
    public String refreshToken;

    @Builder
    public RefreshResponseDto(String accessToken, String refreshToken){
        this.accessToken=accessToken;
        this.refreshToken=refreshToken;
    }
}
