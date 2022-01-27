package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.QPost;
import com.ewha.devookserver.domain.post.QPostTag;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserRecommRepository {

  private final JPAQueryFactory jpaQueryFactory;
  QPost qPost = new QPost("m");
  QPostTag qPostTag = new QPostTag("p");

  public List<PostTag> findAllTagsByPost(int post_postIdx) {

    return jpaQueryFactory.selectFrom(qPostTag)
        .where(qPostTag.post_postIdx.eq(post_postIdx))
        .fetch();
  }
}
