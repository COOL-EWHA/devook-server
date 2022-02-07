package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.Notification;
import com.ewha.devookserver.repository.NotificationRepository;
import java.time.LocalDate;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationService {

  private final NotificationRepository notificationRepository;

  // 사용자의 idx, bookmarkIdx, isPost를 넣으면 그것에 해당하는 dueDate를 반환해주는 함수

  public Notification returnDueDate(Long bookmarkIdx, Long userIdx, Boolean isPost) {
    if (notificationRepository.existsByUserIdxAndPostIdx(userIdx, bookmarkIdx)) {
      return notificationRepository.findByUserIdxAndPostIdx(userIdx, bookmarkIdx);
    }
    if (notificationRepository.existsByUserIdxAndBookmarkIdx(userIdx, bookmarkIdx)) {
      return notificationRepository.findByUserIdxAndBookmarkIdx(userIdx, bookmarkIdx);
    }
    return null;
  }

  // notification 새로 생성, 혹은 정보 수정하는 함수

  @Transactional
  public Boolean saveNotification(Long bookmarkIdx, Long userIdx, Boolean isPost,
      LocalDate dueDate) {
    if (isPost.booleanValue() == true) {

      if (!notificationRepository.existsByUserIdxAndPostIdx(userIdx, bookmarkIdx)) {
        Notification notification = Notification.builder()
            .postIdx(bookmarkIdx)
            .bookmarkIdx(null)
            .dueDate(dueDate)
            .alertTime(null)
            .isPost(true)
            .userIdx(userIdx)
            .build();
        notificationRepository.save(notification);
        return true;
      } else {
        Notification notification = notificationRepository.findByUserIdxAndPostIdx(userIdx,
            bookmarkIdx);

        if (dueDate != null) {
          notification.setDueDate(dueDate);
        }
        notificationRepository.save(notification);
        return true;
      }
    }

    if (isPost.booleanValue() == false) {
      if (!notificationRepository.existsByUserIdxAndBookmarkIdx(userIdx, bookmarkIdx)) {

        Notification notification = Notification.builder()
            .postIdx(null)
            .bookmarkIdx(bookmarkIdx)
            .dueDate(dueDate)
            .alertTime(null)
            .isPost(false)
            .userIdx(userIdx)
            .build();

        notificationRepository.save(notification);
        return true;
      } else {
        Notification notification = notificationRepository.findByUserIdxAndBookmarkIdx(userIdx,
            bookmarkIdx);
        if (dueDate != null) {
          notification.setDueDate(dueDate);
        }
        notificationRepository.save(notification);
        return true;
      }
    }

    return false;
  }
}
