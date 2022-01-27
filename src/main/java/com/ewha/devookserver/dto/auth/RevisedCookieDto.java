package com.ewha.devookserver.dto.auth;


import lombok.Builder;
import lombok.Getter;

@Getter
public class RevisedCookieDto {
  private final String email;
  private final String nickname;
  private final String accessToken;

  @Builder
  public RevisedCookieDto(String email, String nickname, String accessToken){
    this.email=email;
    this.nickname=nickname;
    this.accessToken=accessToken;
  }

}
