package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.QPost;
import com.ewha.devookserver.domain.post.QPostTag;
import com.ewha.devookserver.domain.post.RefrenceDto;
import com.ewha.devookserver.service.PostService;
import com.ewha.devookserver.service.RecommendService;
import com.ewha.devookserver.service.UserBookmarkService;
import com.ewha.devookserver.service.UserRecommService;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.lang.ref.Reference;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class QueryRepository {

  private final JPAQueryFactory jpaQueryFactory;
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final UserRecommService userRecommService;
  private final UserBookmarkService userBookmarkService;
  private final UserBookmarkRepository userBookmarkRepository;
  QPost qPost = new QPost("m");
  QPostTag qPostTag = new QPostTag("p");

  public List<Post> searchEngine(List<Post> postList, String question) {
    System.out.println("searchEngine for  " + question);
    List<Post> searchResult = new ArrayList<>();

    for (Post post : postList) {

      if (post.getPostTitle().toLowerCase().contains(question.toLowerCase())
          || post.getPostDescription().toLowerCase().contains(question.toLowerCase())) {
        System.out.println(post.getPostIdx());
        if (post.getPostTitle().contains(question)) {
          System.out.println("title 일치" + question);
        }
        if (post.getPostDescription().toLowerCase().contains(question.toLowerCase())) {
          System.out.println("description 일치" + question);
        }
        searchResult.add(post);
        continue;
      }

      List<PostTag> postTagList = findAllTagsByPost(post.getPostIdx().intValue());

      for (PostTag postTag : postTagList) {
        if (postTag.getPostTagName().equalsIgnoreCase(question)) {
          System.out.println(postTag.getPost_postIdx() + "태그에서 일치");
          searchResult.add(post);
          break;
        }
      }
    }

    List<Long> exampleResult = new ArrayList<>();
    for (Post post : searchResult) {
      exampleResult.add(post.getPostIdx());
    }
    System.out.println(exampleResult);

    return searchResult;
  }


  public List<PostTag> findAllTagsByPost(int post_postIdx) {

    return jpaQueryFactory.selectFrom(qPostTag)
        .where(qPostTag.post_postIdx.eq(post_postIdx))
        .fetch();
  }

  public Object returnNowPost(Long id, String userIdx) {
    // post
    if (postRepository.existsByPostIdxAndUserIdx(id, userIdx)) {
      return postRepository.findByPostIdxAndUserIdx(id, userIdx);
    }
    if (userBookmarkRepository.existsUserBookmarkByPostIdxAndUserIdx(id, Long.valueOf(userIdx))) {
      return userBookmarkRepository.findByPost_postIdxAndUser_userIdx(id, Long.valueOf(userIdx));
    }

    // bookmarkId
    return null;
  }

  // findAllByPostIdx
  public List<Post> findAllByPostIdxFunction1(Pageable page, String userIdx, String question) {

    if (question == null) {
      System.out.println("비어있으면 그냥 전체 리턴");

      List<Post> postList = userBookmarkService.bookmarkExcludeUserPosts(Long.valueOf(userIdx));
      Collections.sort(postList);
      return postList;
    }
    System.out.println("태그가 없을때, question이 주어진 경우+fucntion1");

    /*
    List<Post> getList=postRepository.findAll();
    List<Post> filteredPostList = new ArrayList<>();

    for(Post post : getList){
      if(post.getUserIdx().equals(userIdx)){
        filteredPostList.add(post);
      }
    }

     */

    List<Post> filteredPostList = userBookmarkService.bookmarkExcludeUserPosts(
        Long.valueOf(userIdx));

    Collections.sort(filteredPostList);
    filteredPostList = searchEngine(filteredPostList, question);
    return filteredPostList.stream().collect(Collectors.toList());
  }

  // findAllByPostIdxDesc
  public List<Post> findAllByPostIdxDescFunction2(Long id, Pageable page, String userIdx,
      String question) {

    Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());
    System.out.println(createdAt);

    if (postRepository.existsByPostIdxAndUserIdx(id, userIdx)) {
      createdAt = postRepository.findByPostIdxAndUserIdx(id, userIdx).getCreatedAt();
    }
    if (userBookmarkRepository.existsUserBookmarkByPostIdxAndUserIdx(id, Long.valueOf(userIdx))) {

      createdAt = userBookmarkRepository.findByPost_postIdxAndUser_userIdx(id,
          Long.valueOf(userIdx)).getCreatedAt();
    }

    if (question == null) {
      System.out.println("여기?");

      List<Post> postList = userBookmarkService.bookmarkExcludeUserPosts(Long.valueOf(userIdx));
      List<Post> returnList = new ArrayList<>();
      for (Post post : postList) {
        if (post.getPostIdx() < id && post.getCreatedAt().before(createdAt)) {
          returnList.add(post);
        }
      }
      Collections.sort(returnList);
      return returnList.stream().collect(Collectors.toList());

    }
    System.out.println("태그가 없을때, question이 주어진 경우+function2");

    List<Post> getList = userBookmarkService.bookmarkExcludeUserPosts(Long.valueOf(userIdx));

    List<Post> filteredPostList = new ArrayList<>();

    for (Post post : getList) {
      if (post.getPostIdx() < id && post.getCreatedAt().before(createdAt)) {
        filteredPostList.add(post);
      }
    }

    Collections.sort(filteredPostList);
    filteredPostList = searchEngine(filteredPostList, question);
    return filteredPostList.stream().collect(Collectors.toList());
  }

  // 태그 리스트 필터링하는 함수
  public List<Post> tagFiltering(List<Long> postIdxList, String userIdx, String question) {

    List<Post> getList = userBookmarkService.bookmarkExcludeUserPosts(Long.valueOf(userIdx));
    List<Post> filteredPostList = new ArrayList<>();

    for (Post post : getList) {
      if (postIdxList.contains(post.getPostIdx())) {
        filteredPostList.add(post);
      }
    }

    if (question != null) {
      filteredPostList = searchEngine(filteredPostList, question);
    }

    Collections.sort(filteredPostList);
    System.out.println(Arrays.stream(filteredPostList.toArray()).iterator());
    return filteredPostList.stream().collect(Collectors.toList());
  }

  public List<Post> tagFiltering2(List<Long> postIdxList, Long id, String userIdx,
      String question) {

    Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());
    System.out.println(createdAt);

    if (postRepository.existsByPostIdxAndUserIdx(id, userIdx)) {
      createdAt = postRepository.findByPostIdxAndUserIdx(id, userIdx).getCreatedAt();
    }
    if (userBookmarkRepository.existsUserBookmarkByPostIdxAndUserIdx(id, Long.valueOf(userIdx))) {

      createdAt = userBookmarkRepository.findByPost_postIdxAndUser_userIdx(id,
          Long.valueOf(userIdx)).getCreatedAt();
    }

    List<Post> getList = userBookmarkService.bookmarkExcludeUserPosts(Long.valueOf(userIdx));
    List<Post> filteredPostList = new ArrayList<>();

    for (Post post : getList) {
      // TODO 여기 참고
      if (postIdxList.contains(post.getPostIdx()) && post.getPostIdx() < id && post.getCreatedAt()
          .before(createdAt)) {
        filteredPostList.add(post);
      }
    }

    if (question != null) {
      filteredPostList = searchEngine(filteredPostList, question);
    }

    Collections.sort(filteredPostList);
    return filteredPostList.stream().collect(Collectors.toList());
  }


  public List<Post> recommendPost1(Pageable page, List<RefrenceDto> refrenceDtos, int limit,
      String userIdx) {

    System.out.println("recommendPost1");
    List<Post> resultList = new ArrayList<>();
    Collections.sort(refrenceDtos);

    for (RefrenceDto refrenceDto : refrenceDtos) {
      if (!Objects.equals(refrenceDto.getPost().getUserIdx(), userIdx)) {
        resultList.add(refrenceDto.getPost());
      }
    }

    List<Post> testFilteredPostList = new ArrayList<>();
    for (Post post : resultList) {
      if (!testFilteredPostList.stream()
          .anyMatch(s -> s.getPostUrl().equals(post.getPostUrl()))) {
        testFilteredPostList.add(post);
      }
    }

    return testFilteredPostList.stream().limit(limit).collect(Collectors.toList());
  }

  public List<Post> recommendPost2(Long id, Pageable page, List<RefrenceDto> refrenceDtos,
      int limit, String userIdx) {
    System.out.println("recommendPost2");

    List<Post> resultList = new ArrayList<>();

    Collections.sort(refrenceDtos);

    for (RefrenceDto refrenceDto : refrenceDtos) {
      if (refrenceDto.getPost().getPostIdx() < id && !Objects.equals(
          refrenceDto.getPost().getUserIdx(),
          userIdx)) {
        resultList.add(refrenceDto.getPost());
      }
    }

    List<Post> testFilteredPostList = new ArrayList<>();
    for (Post post : resultList) {
      if (!testFilteredPostList.stream()
          .anyMatch(s -> s.getPostUrl().equals(post.getPostUrl()))) {
        testFilteredPostList.add(post);
      }
    }

    return testFilteredPostList.stream().limit(limit).collect(Collectors.toList());
  }

  // 태그 리스트 필터링하는 함수
  public List<Post> tagFilteringRecommendUser1(List<Long> postIdxList, String userIdx,
      String question, boolean isUser, List<String> requiredTagList, int limit) {

    System.out.println("들어왔다.");
    List<Post> getList = postRepository.findAll();
    List<Post> filteredPostList = new ArrayList<>();
    List<RefrenceDto> resultArray = new ArrayList<>();

    int count;
    if (requiredTagList.isEmpty()) {
      requiredTagList = userRecommService.getPostTagList(userIdx);
      for (Post post : getList) {
        count = 0;

        List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(
            post.getPostIdx().intValue());
        if (!post.getUserIdx().equals(userIdx)
        ) {

          for (PostTag postTag : eachPostTagList) {
            for (String string : requiredTagList) {
              if (postTag.getPostTagName().equals(string)) {
                count++;
              }
            }
          }

          RefrenceDto refrenceDto = new RefrenceDto();
          refrenceDto.setPost(post);
          refrenceDto.setRefrence(count);
          resultArray.add(refrenceDto);

        }
      }
      Collections.sort(resultArray);

      for (RefrenceDto refrenceDto : resultArray) {
        filteredPostList.add(refrenceDto.getPost());
      }

      if (question != null) {
        filteredPostList = searchEngine(filteredPostList, question);
      }

      return filteredPostList.stream().limit(limit).collect(Collectors.toList());

    }

    for (Post post : getList) {
      count = 0;

      List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(
          post.getPostIdx().intValue());
      if (postIdxList.contains(post.getPostIdx()) && !post.getUserIdx().equals(userIdx)) {
        // filteredPostList.add(post);
        for (PostTag postTag : eachPostTagList) {
          for (String string : requiredTagList) {
            if (postTag.getPostTagName().equals(string)) {
              count++;
            }
          }
        }

        RefrenceDto refrenceDto = new RefrenceDto();
        refrenceDto.setPost(post);
        refrenceDto.setRefrence(count);
        resultArray.add(refrenceDto);


      }
    }
    Collections.sort(resultArray);

    if (question != null) {
      filteredPostList = searchEngine(filteredPostList, question);
    }

    return filteredPostList.stream().collect(Collectors.toList());
  }


  // requiredTagList = 검색 필터링(옆에 있는 배너) 용도 -> 따라서 '사용자' 추천이 아님! 함수를 새로 만들어주어야 한다.
  public List<Post> tagFilteringRecommendUser2(List<Long> postIdxList, Long id, String userIdx,
      String question, boolean isUser, List<String> requiredTagList, int limit) {
    System.out.println("들어왔다22.");
    //TODO
    Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());

    List<Post> getList = postRepository.findAll();
    List<Post> filteredPostList = new ArrayList<>();
    List<RefrenceDto> resultArray = new ArrayList<>();
    List<String> userPostTag = userRecommService.getPostUserTagList(userIdx);

    for(String string:userPostTag){
      System.out.println(string);
    }

    int count;

    if (requiredTagList.isEmpty()) {
      for (Post post : getList) {
        count = 0;

        // 해당 post의 tag 리스트
        List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(
            post.getPostIdx().intValue());

        // 사용자의 태그를 가져와서,
        // 만약 사용자의 태그와 해당 post의 이름이 일치하면
        // count 를 추가하는 방식으로 수정하기

        if (!post.getUserIdx().equals(userIdx) && post.getPostIdx() < id) {

          for(PostTag postTag:eachPostTagList){
            for(String string:userPostTag){
              if(postTag.getPostTagName().equals(string)){
                count+=10;
              }
            }
          }

          for(String string:userPostTag){
            if(post.getPostTitle().contains(string)){
              count+=3;
            }
            if(post.getPostDescription().contains(string)){
              count+=1;
            }
          }

          /*
          for (PostTag postTag : eachPostTagList) {
            for (String string : requiredTagList) {
              if (postTag.getPostTagName().equals(string)) {
                count+=10;
              }
            }
          }

           */



          RefrenceDto refrenceDto = new RefrenceDto();
          refrenceDto.setPost(post);
          refrenceDto.setRefrence(count);
          resultArray.add(refrenceDto);

        }
      }


      Collections.sort(resultArray);

      for(RefrenceDto refrenceDto : resultArray){
        System.out.println(refrenceDto.getRefrence());
      }

      for (RefrenceDto refrenceDto : resultArray) {
        filteredPostList.add(refrenceDto.getPost());
      }

      if (question != null) {
        filteredPostList = searchEngine(filteredPostList, question);
      }

      List<Post> testFilteredPostList = new ArrayList<>();
      for (Post post : filteredPostList) {
        if (!testFilteredPostList.stream()
            .anyMatch(s -> s.getPostUrl().equals(post.getPostUrl()))) {
          testFilteredPostList.add(post);
        }
      }

      return filteredPostList.stream().limit(limit).collect(Collectors.toList());

    }

    for (Post post : getList) {
      count = 0;

      List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(
          post.getPostIdx().intValue());
      if (postIdxList.contains(post.getPostIdx()) && !post.getUserIdx().equals(userIdx)
          && post.getPostIdx() < id) {

        for (PostTag postTag : eachPostTagList) {
          for (String string : requiredTagList) {
            if (postTag.getPostTagName().equals(string)) {
              count++;
            }
          }
        }

        RefrenceDto refrenceDto = new RefrenceDto();
        refrenceDto.setPost(post);
        refrenceDto.setRefrence(count);
        resultArray.add(refrenceDto);

      }
    }
    Collections.sort(resultArray);

    for (RefrenceDto refrenceDto : resultArray) {
      filteredPostList.add(refrenceDto.getPost());
    }

    if (question != null) {
      filteredPostList = searchEngine(filteredPostList, question);
    }

    List<Post> testFilteredPostList = new ArrayList<>();
    for (Post post : filteredPostList) {
      if (!testFilteredPostList.stream()
          .anyMatch(s -> s.getPostUrl().equals(post.getPostUrl()))) {
        testFilteredPostList.add(post);
      }
    }

    return filteredPostList.stream().limit(limit).collect(Collectors.toList());
  }

  // 태그 리스트 필터링하는 함수
  public List<Post> tagFilteringRecommendNotUser1(List<Long> postIdxList,
      String question, boolean isUser, List<String> requiredTagList, int limit) {

    List<Post> getList = postRepository.findAll();
    List<Post> filteredPostList = new ArrayList<>();
    List<RefrenceDto> resultArray = new ArrayList<>();

    int count;
    if (requiredTagList.isEmpty()) {
      requiredTagList = userRecommService.getAllTagList();
      for (Post post : getList) {
        count = 0;

        List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(
            post.getPostIdx().intValue());

        for (PostTag postTag : eachPostTagList) {
          for (String string : requiredTagList) {
            if (postTag.getPostTagName().equals(string)) {
              count++;
            }
          }
        }

        RefrenceDto refrenceDto = new RefrenceDto();
        refrenceDto.setPost(post);
        refrenceDto.setRefrence(count);
        resultArray.add(refrenceDto);


      }
      Collections.sort(resultArray);

      for (RefrenceDto refrenceDto : resultArray) {
        filteredPostList.add(refrenceDto.getPost());
      }

      if (question != null) {
        filteredPostList = searchEngine(filteredPostList, question);
      }

      List<Post> testFilteredPostList = new ArrayList<>();
      for (Post post : filteredPostList) {
        if (!testFilteredPostList.stream()
            .anyMatch(s -> s.getPostUrl().equals(post.getPostUrl()))) {
          testFilteredPostList.add(post);
        }
      }

      return testFilteredPostList.stream().limit(limit).collect(Collectors.toList());
    }

    for (Post post : getList) {
      count = 0;

      List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(
          post.getPostIdx().intValue());
      if (postIdxList.contains(post.getPostIdx())) {
        for (PostTag postTag : eachPostTagList) {
          for (String string : requiredTagList) {
            if (postTag.getPostTagName().equals(string)) {
              count++;
            }
          }
        }

        RefrenceDto refrenceDto = new RefrenceDto();
        refrenceDto.setPost(post);
        refrenceDto.setRefrence(count);
        resultArray.add(refrenceDto);


      }
    }
    Collections.sort(resultArray);

    if (question != null) {
      filteredPostList = searchEngine(filteredPostList, question);
    }

    List<Post> testFilteredPostList = new ArrayList<>();
    for (Post post : filteredPostList) {
      if (!testFilteredPostList.stream()
          .anyMatch(s -> s.getPostUrl().equals(post.getPostUrl()))) {
        testFilteredPostList.add(post);
      }
    }

    return testFilteredPostList.stream().limit(limit).collect(Collectors.toList());
  }

  public List<Post> tagFilteringRecommendNotUser2(List<Long> postIdxList, Long id,
      String question, boolean isUser, List<String> requiredTagList, int limit) {

    List<Post> getList = postRepository.findAll();
    List<Post> filteredPostList = new ArrayList<>();
    List<RefrenceDto> resultArray = new ArrayList<>();

    int count;

    if (requiredTagList.isEmpty()) {
      requiredTagList = userRecommService.getAllTagList();
      for (Post post : getList) {
        count = 0;

        List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(
            post.getPostIdx().intValue());
        if (
            post.getPostIdx() < id) {

          for (PostTag postTag : eachPostTagList) {
            for (String string : requiredTagList) {
              if (postTag.getPostTagName().equals(string)) {
                count++;
              }
            }
          }

          RefrenceDto refrenceDto = new RefrenceDto();
          refrenceDto.setPost(post);
          refrenceDto.setRefrence(count);
          resultArray.add(refrenceDto);

        }
      }
      Collections.sort(resultArray);

      for (RefrenceDto refrenceDto : resultArray) {
        filteredPostList.add(refrenceDto.getPost());
      }

      if (question != null) {
        filteredPostList = searchEngine(filteredPostList, question);
      }

      List<Post> testFilteredPostList = new ArrayList<>();
      for (Post post : filteredPostList) {
        if (!testFilteredPostList.stream()
            .anyMatch(s -> s.getPostUrl().equals(post.getPostUrl()))) {
          testFilteredPostList.add(post);
        }
      }

      return testFilteredPostList.stream().limit(limit).collect(Collectors.toList());
    }

    for (Post post : getList) {
      count = 0;

      List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(
          post.getPostIdx().intValue());
      if (postIdxList.contains(post.getPostIdx())
          && post.getPostIdx() < id) {

        for (PostTag postTag : eachPostTagList) {
          for (String string : requiredTagList) {
            if (postTag.getPostTagName().equals(string)) {
              count++;
            }
          }
        }

        RefrenceDto refrenceDto = new RefrenceDto();
        refrenceDto.setPost(post);
        refrenceDto.setRefrence(count);
        resultArray.add(refrenceDto);

      }
    }
    Collections.sort(resultArray);

    for (RefrenceDto refrenceDto : resultArray) {
      filteredPostList.add(refrenceDto.getPost());
    }

    if (question != null) {
      filteredPostList = searchEngine(filteredPostList, question);
    }

    List<Post> testFilteredPostList = new ArrayList<>();
    for (Post post : filteredPostList) {
      if (!testFilteredPostList.stream()
          .anyMatch(s -> s.getPostUrl().equals(post.getPostUrl()))) {
        testFilteredPostList.add(post);
      }
    }

    return testFilteredPostList.stream().limit(limit).collect(Collectors.toList());
  }
}
