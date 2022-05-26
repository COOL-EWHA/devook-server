package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.UserBookmark;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBookmarkRepository extends JpaRepository<UserBookmark, Long> {

  @Query("select p from UserBookmark p where p.postIdx=?1 and p.userIdx=?2")
  UserBookmark existsByPost_postIdxAndUser_userIdx(Long post_postIdx, Long user_userIdx);

  @Query("select p from UserBookmark p where p.userIdx=?1")
  List<UserBookmark> findAllByUser_userIdx(Long user_userIdx);

  @Query("select p from UserBookmark p where p.postIdx=?1 and p.userIdx=?2")
  UserBookmark findByPost_postIdxAndUser_userIdx(Long post_postIdx, Long user_userIdx);

  Boolean existsUserBookmarkByPostIdxAndUserIdx(Long postIdx, Long userIdx);

  @Transactional
  void deleteAllByPostIdx(Long postIdx);

  int countAllByUserIdx(Long userIdx);
}
