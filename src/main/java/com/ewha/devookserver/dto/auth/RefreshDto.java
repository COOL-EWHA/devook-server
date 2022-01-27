package com.ewha.devookserver.dto.auth;

import com.ewha.devookserver.domain.user.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RefreshDto {

  public String accessToken;
  public String refreshToken;
  public Member member;

  @Builder
  public RefreshDto(String accessToken, String refreshToken, Member member) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.member = member;
  }
}
