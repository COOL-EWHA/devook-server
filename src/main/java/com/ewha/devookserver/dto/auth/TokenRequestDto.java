package com.ewha.devookserver.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TokenRequestDto {

  public String code;

  @Builder
  public TokenRequestDto(String code) {
    this.code = code;
  }
}
