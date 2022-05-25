package com.ewha.devookserver.dto.device;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OnesignalAlarmRequestDto {

  public String message;
  public String userIdx;
}
