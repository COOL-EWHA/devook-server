package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.user.UserBookmark;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBookmarkRepository extends JpaRepository<UserBookmark, Long> {

  @Query("select p from UserBookmark p where p.post_postIdx=?1 and p.user_userIdx=?2")
  UserBookmark existsByPost_postIdxAndUser_userIdx(Long post_postIdx, Long user_userIdx);

  // 유저에 해당하는 UserBookmark 전체 돌려주기

  @Query("select p from UserBookmark p where p.user_userIdx=?1")
  List<UserBookmark> findAllByUser_userIdx(Long user_userIdx);

}
