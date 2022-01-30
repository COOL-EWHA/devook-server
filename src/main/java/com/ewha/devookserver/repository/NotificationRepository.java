package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.Notification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  @Query("select p from Notification p where p.post_postIdx=?1 and p.user_userIdx=?2 and p.isPost=?3")
  Optional<Notification> getNotification(Long post_postIdx, Long user_userIdx, Boolean isPost);
}
