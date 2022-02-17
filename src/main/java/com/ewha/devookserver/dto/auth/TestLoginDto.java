package com.ewha.devookserver.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TestLoginDto {

  public String refreshToken;

  @Builder
  public TestLoginDto(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
