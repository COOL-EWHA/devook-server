package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.PostTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<PostTag, Long> {

  List<PostTag> findAllByPostTagName(String postTagName);

  @Query("select p from PostTag p where p.post_postIdx=?1")
  List<PostTag> findAllByPost_postIdx(int postIdx);

}
