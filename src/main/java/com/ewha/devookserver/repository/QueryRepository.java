package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.QPost;
import com.ewha.devookserver.domain.post.QPostTag;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class QueryRepository {

  QPost qPost = new QPost("m");
  QPostTag qPostTag = new QPostTag("p");

  private final JPAQueryFactory jpaQueryFactory;
  private final PostRepository postRepository;



  // findAllByPostIdx (PostRepository)
  public List<Post> findAllPostByIdx() {
    return jpaQueryFactory.selectFrom(qPost)
        .where(qPost.userIdx.eq("64"))
        .orderBy(qPost.postIdx.desc())
        .fetch();
  }

  public List<PostTag> findAllTagsByPost(int post_postIdx) {

    return jpaQueryFactory.selectFrom(qPostTag)
        .where(qPostTag.post_postIdx.eq(post_postIdx))
        .fetch();
  }

  // 추가

  // findAllByPostIdx
  public List<Post> findAllByPostFunction1(Pageable pageable, String userIdx) {

    return jpaQueryFactory.selectFrom(qPost)
        .where(qPost.userIdx.eq(userIdx))
        .orderBy(qPost.postIdx.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
  }


  // findAllByPostIdx
  public List<Post> findAllByPostIdxFunction1(Pageable page, String userIdx, String question) {

    if (question.equals("")||question.equals(null)) {
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
  public List<Post> findAllByPostIdxDescFunction2(Long id, Pageable page, String userIdx,
      String question) {

    if (question.equals(null)) {
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
        .fetch();
  }

  // 태그 리스트 필터링하는 함수
  public List<Post> tagFiltering(List<Long> postIdxList, String userIdx, String question) {

    List<Post> getList = postRepository.findAll();
    List<Post> filteredPostList = new ArrayList<>();

    for (Post post : getList) {
      if (postIdxList.contains(post.getPostIdx()) && post.getUserIdx().equals(userIdx)) {
        filteredPostList.add(post);
      }
    }
    Collections.sort(filteredPostList);
    return filteredPostList.stream().limit(10).collect(Collectors.toList());
  }

  public List<Post> tagFiltering2(List<Long> postIdxList, Long id, String userIdx, String question){

    List<Post> getList = postRepository.findAll();
    List<Post> filteredPostList = new ArrayList<>();

    for (Post post : getList) {

      if (postIdxList.contains(post.getPostIdx()) && post.getUserIdx().equals(userIdx)&&post.getPostIdx()<id) {
        filteredPostList.add(post);
      }
    }
    Collections.sort(filteredPostList);
    return filteredPostList.stream().limit(10).collect(Collectors.toList());
  }

  /*
  public List<Post> tagFilteringFunction2(List<Post> getList, List<Long> postIdxList, Long id,
      String userIdx) {

    List<Post> filteredPostList = new ArrayList<>();


  }

   */
}
