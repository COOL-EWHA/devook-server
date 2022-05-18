package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.Post;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  boolean existsByPostUrl(String url);

  boolean existsByPostIdx(Long postIdx);

  boolean existsByPostIdxAndUserIdx(Long postIdx, String userIdx);

  boolean existsByPostTitle(String title);

  Post getPostByPostIdx(Long postIdx);

  Post getPostByPostTitle(String postTitle);

  Post getPostByPostUrlAndUserIdx(String postUrl, String userIdx);

  Post getFirstByPostTitle(String postTitle);

  Integer countAllByUserIdx(String userIdx);

  @Transactional
  void deletePostByPostIdx(Long postIdx);

  @Query("select p from Post p")
  List<Post> findWithPagination(Pageable pageable);

  @Query("select p from Post p where p.userIdx=?1 order by p.postIdx DESC")
  List<Post> findAllByPostIdx(Pageable page, String userIdx);

  @Query("select p from Post p where p.postIdx<?1 and p.userIdx=?2 order by p.postIdx DESC")
  List<Post> findAllByPostIdxDesc(Long id, Pageable page, String userIdx);

  @Query("select p from Post p where p.postIdx>1226 and p.postIdx<3413")
  List<Post> returnBasicList();

  List<Post> findAllByUserIdx(String userIdx);

  Post findByPostIdxAndUserIdx(Long postIdx, String userIdx);

  Post getPostByPostUrlAndPostDescription(String url, String description);


  Post findFirstByOrderByPostIdxDesc();

  Post findByPostTitle(String title);

}
