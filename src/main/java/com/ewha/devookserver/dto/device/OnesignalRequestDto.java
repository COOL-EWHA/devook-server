package com.ewha.devookserver.dto.device;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnesignalRequestDto {
  public String app_id;
  public String device_type;
  public String identifier;
}
