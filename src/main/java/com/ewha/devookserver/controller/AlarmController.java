package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.Alarm;
import com.ewha.devookserver.dto.post.AlarmPatchRequestDto;
import com.ewha.devookserver.dto.post.AlarmResponseDto;
import com.ewha.devookserver.repository.AlarmRepository;
import com.ewha.devookserver.service.AlarmService;
import com.ewha.devookserver.service.OauthService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AlarmController {

  private final OauthService oauthService;
  private final AlarmService alarmService;
  private final AlarmRepository alarmRepository;


  @GetMapping("/notifications")
  public ResponseEntity<List<AlarmResponseDto>> getNotification(
      @RequestParam(name = "cursor", required = false) Long cursor,
      @RequestParam(name = "limit", required = false) Long limit,
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    if (!oauthService.isUserExist(accessToken)) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }    // 유저 예외처리 완료
    String userIdx = oauthService.getUserIdx(accessToken);

    if (cursor == null) {
      cursor = 100000L;
    }
    if (limit == null) {
      limit = 10L;
    }
    List<AlarmResponseDto> finalResult = alarmService.returnAlarmCursorList(Long.valueOf(userIdx),
        cursor, limit.intValue());
    return ResponseEntity.status(200).body(finalResult);
  }


  @PatchMapping("/notifications/{id}")
  public ResponseEntity<String> editNotification(
      @PathVariable(name = "id") Long id,
      @RequestBody AlarmPatchRequestDto alarmPatchRequestDto,
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      return ResponseEntity.status(401).body("1");
    }
    if (!oauthService.isUserExist(accessToken)) {
      return ResponseEntity.status(401).body("2");
    }    // 유저 예외처리 완료

    Alarm alarm = alarmRepository.findByAlarmIdx(id);
    if (alarm == null) {
      return ResponseEntity.status(404).body("ok");
    }

    Boolean setIsRead = alarmPatchRequestDto.getIsRead();

    alarm.setIsRead(setIsRead);
    alarmRepository.save(alarm);

    return ResponseEntity.status(200).body("ok");
  }
}
