package com.ewha.devookserver.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TestLoginDto {
  public String email;

  @Builder
  public TestLoginDto(String email){
    this.email=email;
  }
}
