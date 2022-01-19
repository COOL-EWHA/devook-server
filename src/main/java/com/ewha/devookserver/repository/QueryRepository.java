package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.QPost;
import com.ewha.devookserver.domain.post.QPostTag;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
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


  // 추가

  // findAllByPostIdx
  public List<Post> findAllByPostFunction1(Pageable pageable, String userIdx){
    return jpaQueryFactory.selectFrom(qPost)
        .where(qPost.userIdx.eq(userIdx))
        .orderBy(qPost.postIdx.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
  }


  // 여기 아래가 함수.. ㅠ

  // findAllByPostIdx
  public List<Post> findAllByPostIdxFunction1(Pageable page, String userIdx, String question){

    if(question=="") {
      System.out.println("비어있으면 그냥 전체 리턴");

      return jpaQueryFactory.selectFrom(qPost)
          .where(qPost.userIdx.eq(userIdx))
          .orderBy(qPost.postIdx.desc())
          .fetch();
    }
    return jpaQueryFactory.selectFrom(qPost)
        .where(qPost.userIdx.eq(userIdx)
            .and((
                qPost.postTitle.contains(question)
            .or(qPost.postDescription.contains(question)
            ))))
        .orderBy(qPost.postIdx.desc())
        .fetch();
  }

  // findAllByPostIdxDesc
  public List<Post> findAllByPostIdxDescFunction2(Long id, Pageable page, String userIdx, String question) {
    System.out.println(question + "????");

    if (question == "") {
      System.out.println("비어있으면 그냥 전체 리턴");
      return jpaQueryFactory.selectFrom(qPost)
          .where(qPost.postIdx.lt(id).and(qPost.userIdx.eq(userIdx)))
          .orderBy(qPost.postIdx.desc())
          .fetch();
    }
    return jpaQueryFactory.selectFrom(qPost)
        .where(qPost.postIdx.lt(id).and(qPost.userIdx.eq(userIdx))
            .and((
                    qPost.postTitle.contains(question).or(qPost.postDescription.contains(question)
                        ))))
        .orderBy(qPost.postIdx.desc())
        .fetch();  }


}
