/*package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.QPost;
import com.ewha.devookserver.domain.post.QPostTag;
import com.ewha.devookserver.domain.user.UserBookmark;
import com.ewha.devookserver.service.RefrenceDto;
import com.ewha.devookserver.service.UserRecommService;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
  private final TagRepository tagRepository;
  private final UserRecommService userRecommService;
  private final UserBookmarkRepository userBookmarkRepository;



  public List<Post> searchEngine(List<Post> postList, String question) {
    System.out.println("searchEngine for  "+question);
    List<Post> searchResult = new ArrayList<>();

    for (Post post : postList) {

      if (post.getPostTitle().toLowerCase().contains(question.toLowerCase()) || post.getPostDescription().toLowerCase().contains(question.toLowerCase())) {
        System.out.println(post.getPostIdx());
        if(post.getPostTitle().contains(question)){
          System.out.println("title에서 일치"+question);
        }
        if(post.getPostDescription().toLowerCase().contains(question.toLowerCase())){
          System.out.println("description에서 일치"+question);
        }
        searchResult.add(post);
        continue;
      }

      List<PostTag> postTagList = findAllTagsByPost(post.getPostIdx().intValue());

      for (PostTag postTag : postTagList) {
        if (postTag.getPostTagName().toLowerCase().equals(question.toLowerCase())) {
          System.out.println(postTag.getPost_postIdx()+"태그에서 일치");
          searchResult.add(post);
          break;
        }
      }
    }

    List<Long> exampleResult=new ArrayList<>();
    for(Post post:searchResult){
      exampleResult.add(post.getPostIdx());
    }
    System.out.println(exampleResult);


    return searchResult;
  }



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

    System.out.println("findAllByPostIdxFUnctn1");

    List<Post> getList=postRepository.findAll();
    List<Post> filteredPostList = new ArrayList<>();

    if (question==null) {
      System.out.println("비어있으면 그냥 전체 리턴");

      getList= jpaQueryFactory.selectFrom(qPost)
          .where(qPost.userIdx.eq(userIdx))
          .orderBy(qPost.postIdx.desc())
          .fetch();


      for(Post post:getList){

        System.out.println(post.getPostIdx());
      }

      List<UserBookmark> userBookmarkList=userBookmarkRepository.findAllByUser_userIdx(Long.valueOf(userIdx));

      for(UserBookmark userBookmark:userBookmarkList){
        Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
        if(!getList.contains(post)){
          getList.add(post);

        }
      }
      //////////////////////////////
      for(Post post : getList){
        System.out.println(post.getPostIdx());
      }

      List<Post> finalList=new ArrayList<>();


      Collections.sort(getList);

      ////////////////////////////
      return getList;
    }
    System.out.println("태그가 없을때, question이 주어진 경우+fucntion1");


    List<UserBookmark> userBookmarkList=userBookmarkRepository.findAllByUser_userIdx(Long.valueOf(userIdx));

    for(UserBookmark userBookmark:userBookmarkList){
      Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
      if(!getList.contains(post)){
        getList.add(post);

      }
    }
    //////////////////////////////
    for(Post post : getList){
      System.out.println(post.getPostIdx());
    }


    getList = getList.stream().distinct().collect(Collectors.toList());
    System.out.println("정렬 완료");

    for(Post post : getList){
      System.out.println(post.getPostIdx());
    }

    //////////////////////////////


    for(Post post : getList){
      if(post.getUserIdx().equals(userIdx)){
        filteredPostList.add(post);
      }
    }
    for(UserBookmark userBookmark:userBookmarkList){
      Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
      if(!getList.contains(post)){
        getList.add(post);

      }
    }
    Collections.sort(filteredPostList);
    filteredPostList=searchEngine(filteredPostList, question);
    return filteredPostList.stream().limit(10).collect(Collectors.toList());
  }

  // findAllByPostIdxDesc
  public List<Post> findAllByPostIdxDescFunction2(Long id, Pageable page, String userIdx,
      String question) {
    List<Post> getList=new ArrayList<>();
    List<Post> filteredPostList = new ArrayList<>();

    if (question==null) {
      System.out.println("question==null");
      getList= jpaQueryFactory.selectFrom(qPost)
          .where(qPost.postIdx.lt(id).and(qPost.userIdx.eq(userIdx)))
          .orderBy(qPost.postIdx.desc())
          .limit(10)
          .fetch();

      for(Post post:getList){

        System.out.println(post.getPostIdx());
      }
      System.out.println("-------------------------");
      List<UserBookmark> userBookmarkList=userBookmarkRepository.findAllByUser_userIdx(Long.valueOf(userIdx));

      for(UserBookmark userBookmark:userBookmarkList){
        Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
        if(!getList.contains(post)){
          getList.add(post);
        }
      }
      System.out.println("????");
      //////////////////////////////
      for(Post post : getList){
        System.out.println(post.getPostIdx());
      }

      Collections.sort(getList);


      List<Post> finalList=new ArrayList<>();
      int count=0;
      for(Post post : getList){
        count++;
        if(post.getPostIdx()<id&&count<10){
          System.out.println("++++++++"+count);
          finalList.add(post);
        }
      }
      ////////////////////////////
      return finalList;

    }
    System.out.println("태그가 없을때, question이 주어진 경우+function2");


    List<UserBookmark> userBookmarkList=userBookmarkRepository.findAllByUser_userIdx(Long.valueOf(userIdx));

    for(UserBookmark userBookmark:userBookmarkList){
      Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
      if(!getList.contains(post)){
        getList.add(post);

      }
    }
    //////////////////////////////
    for(Post post : getList){
      System.out.println(post.getPostIdx());
    }


    getList = getList.stream().distinct().collect(Collectors.toList());
    System.out.println("정렬 완료");

    for(Post post : getList){
      System.out.println(post.getPostIdx());
    }

    //////////////////////////////

    for(Post post : getList){
      if(post.getUserIdx().equals(userIdx)&&post.getPostIdx()<id){
        filteredPostList.add(post);
      }
    }
    for(UserBookmark userBookmark:userBookmarkList){
      Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());

      if(!filteredPostList.contains(post)){
        filteredPostList.add(post);

      }
    }

    Collections.sort(filteredPostList);
    filteredPostList=searchEngine(filteredPostList, question);
    return filteredPostList.stream().limit(10).collect(Collectors.toList());
  }

  // 태그 리스트 필터링하는 함수
  public List<Post> tagFiltering(List<Long> postIdxList, String userIdx, String question) {
    System.out.println("tagFiltering");

    List<Post> getList = postRepository.findAll();
    List<Post> filteredPostList = new ArrayList<>();

    List<UserBookmark> userBookmarkList=userBookmarkRepository.findAllByUser_userIdx(Long.valueOf(userIdx));

    for(UserBookmark userBookmark:userBookmarkList){
      Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
      if(!filteredPostList.contains(post)){
        getList.add(post);

      }
    }
    //////////////////////////////
    for(Post post : getList){
      System.out.println(post.getPostIdx());
    }


    getList = getList.stream().distinct().collect(Collectors.toList());
    System.out.println("정렬 완료");

    for(Post post : getList){
      System.out.println(post.getPostIdx());
    }

    //////////////////////////////

    for (Post post : getList) {
      if (postIdxList.contains(post.getPostIdx()) && post.getUserIdx().equals(userIdx)) {
        filteredPostList.add(post);
      }
    }

    for(UserBookmark userBookmark:userBookmarkList){
      Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
      if(!filteredPostList.contains(post)){
        filteredPostList.add(post);
      }
    }

    if(question!=null){
      filteredPostList=searchEngine(filteredPostList, question);
    }

    Collections.sort(filteredPostList);
    System.out.println(Arrays.stream(filteredPostList.toArray()).iterator());
    return filteredPostList.stream().limit(10).collect(Collectors.toList());
  }

  public List<Post> tagFiltering2(List<Long> postIdxList, Long id, String userIdx, String question){
    System.out.println("tagFiltering222");

    List<Post> getList = postRepository.findAll();
    List<Post> filteredPostList = new ArrayList<>();

    List<UserBookmark> userBookmarkList=userBookmarkRepository.findAllByUser_userIdx(Long.valueOf(userIdx));

    for(UserBookmark userBookmark:userBookmarkList){

      Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
      if(!getList.contains(post)) {
        getList.add(post);
      }
    }
    //////////////////////////////
    for(Post post : getList){
      System.out.println(post.getPostIdx());
    }


    getList = getList.stream().distinct().collect(Collectors.toList());
    System.out.println("정렬 완료");

    for(Post post : getList){
      System.out.println(post.getPostIdx());
    }

    //////////////////////////////

    for (Post post : getList) {
      if (postIdxList.contains(post.getPostIdx()) && post.getUserIdx().equals(userIdx)&&post.getPostIdx()<id) {
        filteredPostList.add(post);
      }
    }
    for(UserBookmark userBookmark:userBookmarkList){
      Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
      if(!filteredPostList.contains(post)){
        filteredPostList.add(post);

      }
    }



    if(question!=null){
      filteredPostList=searchEngine(filteredPostList, question);
    }

    Collections.sort(filteredPostList);
    return filteredPostList.stream().limit(10).collect(Collectors.toList());
  }





  public List<Post> recommendPost1(Pageable page, List<RefrenceDto> refrenceDtos, int limit, String userIdx){

    System.out.println("recommendPost1");
    List<Post> resultList=new ArrayList<>();
    Collections.sort(refrenceDtos);


    for(RefrenceDto refrenceDto:refrenceDtos){
      if(!Objects.equals(refrenceDto.getPost().getUserIdx(), userIdx)){
        resultList.add(refrenceDto.getPost());
      }
    }

    // post 만 뽑았음

    // sort 가 지금 잘 안먹힌다.
    return resultList.stream().limit(limit).collect(Collectors.toList());
  }

  public List<Post> recommendPost2(Long id, Pageable page, List<RefrenceDto> refrenceDtos, int limit, String userIdx){
    System.out.println("recommendPost2");

    List<Post> resultList=new ArrayList<>();

    Collections.sort(refrenceDtos);

    for(RefrenceDto refrenceDto:refrenceDtos){
      if(refrenceDto.getPost().getPostIdx()<id&& !Objects.equals(refrenceDto.getPost().getUserIdx(),
          userIdx)){
        resultList.add(refrenceDto.getPost());
      }
    }


    return resultList.stream().limit(limit).collect(Collectors.toList());
  }

  // 태그 리스트 필터링하는 함수
  public List<Post> tagFilteringRecommendUser1(List<Long> postIdxList, String userIdx, String question, boolean isUser, List<String> requiredTagList, int limit) {

    System.out.println("들어왔다.");
    List<Post> getList = postRepository.findAll();

    List<UserBookmark> userBookmarkList=userBookmarkRepository.findAllByUser_userIdx(Long.valueOf(userIdx));

    for(UserBookmark userBookmark:userBookmarkList){
      Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
      if(!getList.contains(post)){
        getList.add(post);

      }
    }


    //////////////////////////////
    for(Post post : getList){
      System.out.println(post.getPostIdx());
    }


    getList = getList.stream().distinct().collect(Collectors.toList());
    System.out.println("정렬 완료");

    for(Post post : getList){
      System.out.println(post.getPostIdx());
    }

    //////////////////////////////



    List<Post> filteredPostList = new ArrayList<>();
    List<RefrenceDto> resultArray=new ArrayList<>();

    int count;
    if(requiredTagList.isEmpty()){
      requiredTagList=userRecommService.getPostTagList(userIdx);
      for (Post post : getList) {
        count=0;

        List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(post.getPostIdx().intValue());
        if (!post.getUserIdx().equals(userIdx)
        ) {
          //filteredPostList.add(post);


          for(PostTag postTag:eachPostTagList){
            for(String string:requiredTagList){
              if(postTag.getPostTagName().equals(string)){
                count++;
              }
            }
          }

          RefrenceDto refrenceDto=new RefrenceDto();
          refrenceDto.setPost(post);
          refrenceDto.setRefrence(count);
          resultArray.add(refrenceDto);

        }
      }
      Collections.sort(resultArray);

      for(RefrenceDto refrenceDto:resultArray){
        filteredPostList.add(refrenceDto.getPost());
      }
      for(UserBookmark userBookmark:userBookmarkList){
        Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
        if(!filteredPostList.contains(post)){
          filteredPostList.add(post);
        }
      }
      if(question!=null){
        filteredPostList=searchEngine(filteredPostList, question);
      }

      return filteredPostList.stream().limit(limit).collect(Collectors.toList());

    }


    for (Post post : getList) {
      count=0;

      List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(post.getPostIdx().intValue());
      if (postIdxList.contains(post.getPostIdx()) && !post.getUserIdx().equals(userIdx)) {
        // filteredPostList.add(post);
        for(PostTag postTag:eachPostTagList){
          for(String string:requiredTagList){
            if(postTag.getPostTagName().equals(string)){
              count++;
            }
          }
        }

        RefrenceDto refrenceDto=new RefrenceDto();
        refrenceDto.setPost(post);
        refrenceDto.setRefrence(count);
        resultArray.add(refrenceDto);


      }
    }
    Collections.sort(resultArray);

    if(question!=null){
      filteredPostList=searchEngine(filteredPostList, question);
    }

    System.out.println(Arrays.stream(filteredPostList.toArray()).iterator());
    return filteredPostList.stream().limit(10).collect(Collectors.toList());
  }






  public List<Post> tagFilteringRecommendUser2(List<Long> postIdxList, Long id, String userIdx, String question, boolean isUser, List<String> requiredTagList, int limit) {
    System.out.println("들어왔다22.");



    List<Post> getList = postRepository.findAll();

    List<UserBookmark> userBookmarkList=userBookmarkRepository.findAllByUser_userIdx(Long.valueOf(userIdx));

    for(UserBookmark userBookmark:userBookmarkList){
      Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
      if(!getList.contains(post)){
        getList.add(post);

      }
    }
    //////////////////////////////
    for(Post post : getList){
      System.out.println(post.getPostIdx());
    }


    getList = getList.stream().distinct().collect(Collectors.toList());
    System.out.println("정렬 완료");

    for(Post post : getList){
      System.out.println(post.getPostIdx());
    }

    //////////////////////////////
    List<Post> filteredPostList = new ArrayList<>();
    List<RefrenceDto> resultArray=new ArrayList<>();

    int count;


    if(requiredTagList.isEmpty()){
      requiredTagList=userRecommService.getPostTagList(userIdx);
      for (Post post : getList) {
        count=0;

        List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(post.getPostIdx().intValue());
        if (!post.getUserIdx().equals(userIdx)
            && post.getPostIdx() < id) {
          //filteredPostList.add(post);


          for(PostTag postTag:eachPostTagList){
            for(String string:requiredTagList){
              if(postTag.getPostTagName().equals(string)){
                count++;
              }
            }
          }

          RefrenceDto refrenceDto=new RefrenceDto();
          refrenceDto.setPost(post);
          refrenceDto.setRefrence(count);
          resultArray.add(refrenceDto);

        }
      }
      Collections.sort(resultArray);

      for(RefrenceDto refrenceDto:resultArray){
        filteredPostList.add(refrenceDto.getPost());
      }
      for(UserBookmark userBookmark:userBookmarkList){
        Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
        if(!filteredPostList.contains(post)){
          filteredPostList.add(post);

        }
      }

      if(question!=null){
        filteredPostList=searchEngine(filteredPostList, question);
      }

      return filteredPostList.stream().limit(limit).collect(Collectors.toList());

    }

    for (Post post : getList) {
      count=0;

      List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(post.getPostIdx().intValue());
      if (postIdxList.contains(post.getPostIdx()) && !post.getUserIdx().equals(userIdx)
          && post.getPostIdx() < id) {
        //filteredPostList.add(post);


        for(PostTag postTag:eachPostTagList){
          for(String string:requiredTagList){
            if(postTag.getPostTagName().equals(string)){
              count++;
            }
          }
        }

        RefrenceDto refrenceDto=new RefrenceDto();
        refrenceDto.setPost(post);
        refrenceDto.setRefrence(count);
        resultArray.add(refrenceDto);

      }
    }
    Collections.sort(resultArray);

    for(RefrenceDto refrenceDto:resultArray){
      filteredPostList.add(refrenceDto.getPost());
    }
    for(UserBookmark userBookmark:userBookmarkList){
      Post post=postRepository.getPostByPostIdx(userBookmark.getPost_postIdx());
      if(!filteredPostList.contains(post)){
        filteredPostList.add(post);

      }
    }

    if(question!=null){
      filteredPostList=searchEngine(filteredPostList, question);
    }

    return filteredPostList.stream().limit(limit).collect(Collectors.toList());

  }

}


 */
