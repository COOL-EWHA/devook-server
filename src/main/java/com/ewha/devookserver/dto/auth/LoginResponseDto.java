package com.ewha.devookserver.dto.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginResponseDto {

  private final String email;
  private final String nickname;
  private final String accessToken;
  private final String refreshToken;

}
