package com.ewha.devookserver.controller;

import com.ewha.devookserver.dto.device.DevicePostRequestDto;
import com.ewha.devookserver.dto.device.DevicePostResponseDto;
import com.ewha.devookserver.dto.device.OnesignalRequestDto;
import com.ewha.devookserver.dto.device.OnesignalResponseDto;
import com.ewha.devookserver.service.DeviceService;
import com.ewha.devookserver.service.OauthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class DeviceController {

  private final DeviceService deviceService;
  private final OauthService oauthService;

  @PostMapping("/devices")
  public ResponseEntity<?> postDevice(
      @RequestHeader(value = "Authorization") String accessTokenGet, HttpServletResponse response
      , @RequestBody DevicePostRequestDto devicePostRequestDto) throws JsonProcessingException {

    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      return ResponseEntity.status(401).body("1");
    }
    if (!oauthService.isUserExist(accessToken)) {
      return ResponseEntity.status(401).body(" 2");
    }    // 유저 예외처리 완료

    String userIdx = oauthService.getUserIdx(accessToken);

    String appId = devicePostRequestDto.getAppId();
    String deviceType = devicePostRequestDto.getDeviceType();
    OnesignalRequestDto onesignalRequestDto = new OnesignalRequestDto();
    onesignalRequestDto.setApp_id(appId);
    onesignalRequestDto.setDevice_type(deviceType);
    onesignalRequestDto.setIdentifier(userIdx+ LocalDateTime.now());
    onesignalRequestDto.setLanguage("ko");

    OnesignalResponseDto onesignalResponseDto = deviceService.addDeviceInfo(onesignalRequestDto);

    DevicePostResponseDto devicePostResponseDto = new DevicePostResponseDto();
    devicePostResponseDto.setDeviceId(onesignalResponseDto.getId());

    deviceService.addDeviceId(devicePostResponseDto.getDeviceId(), Long.valueOf(userIdx));

    return ResponseEntity.status(200).body(devicePostResponseDto);
  }

  @DeleteMapping("/devices/{deviceId}")
  public ResponseEntity<?> postDevice(
      @RequestHeader(value = "Authorization") String accessTokenGet,
      @PathVariable(value = "deviceId") String deviceId) {
    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      return ResponseEntity.status(401).body("1");
    }
    if (!oauthService.isUserExist(accessToken)) {
      return ResponseEntity.status(401).body(" 2");
    }    // 유저 예외처리 완료

    String userIdx = oauthService.getUserIdx(accessToken);

    deviceService.deleteDeviceId(deviceId, Long.valueOf(userIdx));

    return ResponseEntity.status(200).body("");

  }
}
