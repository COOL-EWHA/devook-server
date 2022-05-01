package com.ewha.devookserver.controller;

import com.ewha.devookserver.service.DeviceService;
import com.ewha.devookserver.service.OauthService;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class DeviceController {

  private final DeviceService deviceService;
  private final OauthService oauthService;

  @PostMapping("/devices/{player_id}")
  public ResponseEntity<?> postDevice(
      @RequestHeader(value = "Authorization") String accessTokenGet, HttpServletResponse response
      , @PathVariable String player_id) {

    System.out.println(player_id);
/*
    ObjectMapper objectMapper = new ObjectMapper();
    String returnValue = objectMapper.writeValueAsString(devicePostRequestDto);
    System.out.println(returnValue);

 */

    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      return ResponseEntity.status(401).body("1");
    }
    if (!oauthService.isUserExist(accessToken)) {
      return ResponseEntity.status(401).body(" 2");
    }    // 유저 예외처리 완료

    String userIdx = oauthService.getUserIdx(accessToken);

    deviceService.addDeviceId(player_id, Long.valueOf(userIdx));

    /*
    String appId = devicePostRequestDto.getAppId();
    String deviceType = devicePostRequestDto.getDeviceType();
    OnesignalRequestDto onesignalRequestDto = new OnesignalRequestDto();
    onesignalRequestDto.setApp_id(appId);
    onesignalRequestDto.setDevice_type(deviceType);
    onesignalRequestDto.setIdentifier(userIdx + LocalDateTime.now());
    onesignalRequestDto.setLanguage("ko");
    onesignalRequestDto.setNotification_types(1);
    onesignalRequestDto.setGame_version("1");



    OnesignalResponseDto onesignalResponseDto = deviceService.addDeviceInfo(onesignalRequestDto);



    DevicePostResponseDto devicePostResponseDto = new DevicePostResponseDto();
    devicePostResponseDto.setDeviceId(onesignalResponseDto.getId());

    deviceService.addDeviceId(devicePostResponseDto.getDeviceId(), Long.valueOf(userIdx));


     */
    return ResponseEntity.status(200).body("");
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
