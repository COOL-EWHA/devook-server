package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.Notification;
import com.ewha.devookserver.repository.NotificationRepository;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationService {

  private final NotificationRepository notificationRepository;

  // 사용자의 idx, bookmarkIdx, isPost를 넣으면 그것에 해당하는 dueDate를 반환해주는 함수

  public Optional<Notification> returnDueDate(Long bookmarkIdx, Long userIdx, Boolean isPost){
    return notificationRepository.getNotification(bookmarkIdx, userIdx, isPost);
  }



}
