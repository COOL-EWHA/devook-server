package com.ewha.devookserver.domain.dto;

import com.ewha.devookserver.config.auth.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RefreshDto {
    public String accessToken;
    public String refreshToken;
    public Member member;

    @Builder
    public RefreshDto(String accessToken, String refreshToken, Member member){
        this.accessToken=accessToken;
        this.refreshToken=refreshToken;
        this.member=member;
    }
}
