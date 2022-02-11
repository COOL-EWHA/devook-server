package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.Alarm;
import com.ewha.devookserver.domain.post.Notification;
import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.UserBookmark;
import com.ewha.devookserver.dto.post.AlarmResponseDto;
import com.ewha.devookserver.repository.AlarmRepository;
import com.ewha.devookserver.repository.MemberRepository;
import com.ewha.devookserver.repository.NotificationRepository;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.QueryRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AlarmService {

  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final QueryRepository queryRepository;
  private final UserBookmarkRepository userBookmarkRepository;
  private final RecommendService recommendService;
  private final NotificationRepository notificationRepository;
  private final NotificationService notificationService;
  private final UserBookmarkService userBookmarkService;
  private final AlarmRepository alarmRepository;

  public void saveTitlePost(Long userIdx) {
    List<AlarmResponseDto> alarmResponseDtos = new ArrayList<>();
    List<Post> postList = postRepository.findAllByUserIdx(String.valueOf(userIdx));
    List<Long> dueDateAlertList = new ArrayList<>();

    for (Post post : postList) {
      Notification notification = notificationService.returnDueDate(post.getPostIdx(), userIdx,
          true);
      System.out.println(LocalDate.now());

      if (post.getIsRead() == false && notification != null) {
        if (notification.getDueDate().getDayOfMonth() == LocalDate.now().getDayOfMonth()) {
          dueDateAlertList.add(post.getPostIdx());
        }
      }
    }
    List<UserBookmark> bookmarks = userBookmarkRepository.findAllByUser_userIdx(userIdx);

    for (UserBookmark userBookmark : bookmarks) {
      Notification notification = notificationService.returnDueDate(userBookmark.getPostIdx(),
          userIdx, true);
      if (userBookmark.getIsRead() == false && notification != null) {
        if (notification.getDueDate().getDayOfMonth() == LocalDate.now().getDayOfMonth()) {
          dueDateAlertList.add(userBookmark.getPostIdx());
        }
      }
    }

    for (Long userBookmarkIdx : dueDateAlertList) {
      Post post = postRepository.getPostByPostIdx(userBookmarkIdx);

      String title = post.getPostTitle();
      if (title.length() > 39) {
        title = title.substring(0, 39);
        title = title.concat("...");
      }

      Alarm alarm = Alarm.builder()
          .userIdx(Long.valueOf(userIdx))
          .type("due-date")
          .message(
              "\uD83D\uDD14 오늘은 '" + title + "' 의 읽기 마감 기한이에요. 서둘러 읽어주세요! \uD83D\uDE09")
          .isRead(false)
          .postIdx(post.getPostIdx())
          .build();

      alarmRepository.save(alarm);
    }
  }

/*
    - 앱 사용 유도 알림: **21:00**에 알림
    - 북마크 목록 x → **등록된 북마크가 없어요. 북마크를 추가해보세요! 😎**
      - 북마크 목록 o, 안 읽은 글 o → **읽지 않은 북마크가 n개 있어요. 추가한 글을 읽어보세요! 🤩
 */


  public void saveUsageAlert(Long userIdx) {
    List<AlarmResponseDto> alarmResponseDtos = new ArrayList<>();
    List<Post> postList = postRepository.findAllByUserIdx(String.valueOf(userIdx));
    Integer counterUserPost = postRepository.countAllByUserIdx(String.valueOf(userIdx));

    int userIsReadCount = 0;

    // 1. post 부터 count
    // 2. bookmark 도 count
    // 둘이 count 했을때 0 이 나오면 등록된 bookmark가 없는 상황. 1번 메세지 리턴

    Integer postCount = postRepository.countAllByUserIdx(String.valueOf(userIdx));
    Integer bookmarkCount = userBookmarkRepository.countAllByUserIdx(userIdx);

    if (postCount + bookmarkCount == 0) {

      Alarm alarm = Alarm.builder()
          .isRead(false)
          .type("no-bookmarks")
          .postIdx(null)
          .userIdx(userIdx)
          .message("등록된 북마크가 없어요. 북마크를 추가해보세요!\uD83D\uDE0E")
          .build();

      alarmRepository.save(alarm);

    } else {
      for (Post post : postList) {
        if (post.getIsRead() == false) {
          userIsReadCount++;
        }
      }

      List<UserBookmark> bookmarks = userBookmarkRepository.findAllByUser_userIdx(userIdx);

      for (UserBookmark userBookmark : bookmarks) {
        if (userBookmark.getIsRead() == false) {
          userIsReadCount++;
        }
      }

      Alarm alarm = Alarm.builder()
          .isRead(false)
          .type("to-read")
          .postIdx(null)
          .userIdx(userIdx)
          .message("읽지 않은 북마크가 " + userIsReadCount + "개 있어요. 추가한 글을 읽어보세요!\uD83E\uDD29")
          .build();

      alarmRepository.save(alarm);

    }
  }

  public List<AlarmResponseDto> returnAlarmCursorList(Long userIdx, Long cursor, Integer limit) {
    List<Alarm> userAlarm = alarmRepository.findAllByUserIdx(Long.valueOf(userIdx));
    List<AlarmResponseDto> responseDtos = new ArrayList<>();
    SimpleDateFormat formatISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    for (Alarm alarm : userAlarm) {
      if (alarm.getAlarmIdx() < cursor) {
        Date dBconvertedTime = alarm.getCreatedAt();
        String dBCreatedAt = formatISO.format(dBconvertedTime);
        AlarmResponseDto alarmResponseDto = AlarmResponseDto.builder()
            .id(alarm.getAlarmIdx())
            .type(alarm.getType())
            .createdAt(dBCreatedAt)
            .message(alarm.getMessage())
            .isRead(alarm.getIsRead())
            .bookmarkId(alarm.getPostIdx())
            .build();

        responseDtos.add(alarmResponseDto);
      }
    }
    Collections.sort(responseDtos);
    return responseDtos.stream().limit(limit).collect(Collectors.toList());
  }
}
