package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  @Query("select p from Notification p where p.postIdx=?1 and p.userIdx=?2 and p.isPost=?3")
  Notification getNotification(Long post_postIdx, Long user_userIdx, Boolean isPost);

  Boolean existsByUserIdxAndPostIdx(Long userIdx, Long postIdx);

  Notification findByUserIdxAndPostIdx(Long userIdx, Long postIdx);

  Boolean existsByUserIdxAndBookmarkIdx(Long userIdx, Long bookmarkIdx);

  Notification findByUserIdxAndBookmarkIdx(Long userIdx, Long bookmarkIdx);
}
