package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.QPost;
import com.ewha.devookserver.domain.post.QPostTag;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class QueryRepository {

  QPost qPost = new QPost("m");
  QPostTag qPostTag = new QPostTag("p");

  private final JPAQueryFactory jpaQueryFactory;
  public QueryRepository(JPAQueryFactory jpaQueryFactory) {
    this.jpaQueryFactory = jpaQueryFactory;
  }

  // findAllByPostIdx (PostRepository)
  public List<Post> findAllPostByIdx(){
    return jpaQueryFactory.selectFrom(qPost)
        .where(qPost.userIdx.eq("64"))
        .orderBy(qPost.postIdx.desc())
        .fetch();
  }

  public List<PostTag> findAllTagsByPost(int post_postIdx){

    return jpaQueryFactory.selectFrom(qPostTag)
        .where(qPostTag.post_postIdx.eq(post_postIdx))
        .fetch();
  }


}
