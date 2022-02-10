package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.Alarm;
import com.ewha.devookserver.dto.post.AlarmResponseDto;
import com.ewha.devookserver.repository.AlarmRepository;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import com.ewha.devookserver.service.AlarmService;
import com.ewha.devookserver.service.NotificationService;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.PostService;
import com.ewha.devookserver.service.UserBookmarkService;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AlarmController {

  private final PostService postService;
  private final OauthService oauthService;
  private final PostRepository postRepository;
  private final UserBookmarkRepository userBookmarkRepository;
  private final NotificationService notificationService;
  private final UserBookmarkService userBookmarkService;
  private final AlarmService alarmService;
  private final AlarmRepository alarmRepository;


  @GetMapping("/notifications")
  public ResponseEntity<?> getNotification(
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      return ResponseEntity.status(401).body("1");
    }
    System.out.println(oauthService.isUserExist(accessToken));
    if (!oauthService.isUserExist(accessToken)) {
      return ResponseEntity.status(401).body("2");
    }    // 유저 예외처리 완료
    String userIdx = oauthService.getUserIdx(accessToken);

    List<Alarm> userAlarm = alarmRepository.findAllByUserIdx(Long.valueOf(userIdx));
    SimpleDateFormat formatISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    List<AlarmResponseDto> finalResult = new ArrayList<>();
    for (Alarm alarm : userAlarm) {
      Date dBconvertedTime = alarm.getCreatedAt();
      String dBCreatedAt = formatISO.format(dBconvertedTime);
      AlarmResponseDto alarmResponseDto = AlarmResponseDto.builder()
          .id(alarm.getAlarmIdx())
          .createdAt(dBCreatedAt)
          .message(alarm.getMessage())
          .isRead(alarm.getIsRead())
          .bookmarkId(alarm.getPostIdx())
          .build();

      finalResult.add(alarmResponseDto);

    }
    return ResponseEntity.status(200).body(finalResult);

  }
}
