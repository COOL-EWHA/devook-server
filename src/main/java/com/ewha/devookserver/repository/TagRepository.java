package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.PostTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository  extends JpaRepository<PostTag, Long> {
  public List<PostTag> findAllByPostTagName(String postTagName);
}
