package com.ewha.devookserver.dto.device;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DevicePostRequestDto {
  public String appId;
  public String deviceType;
}
