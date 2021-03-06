package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.QPost;
import com.ewha.devookserver.domain.post.QPostTag;
import com.ewha.devookserver.domain.post.RefrenceDto;
import com.ewha.devookserver.service.RecommendService;
import com.ewha.devookserver.service.UserBookmarkService;
import com.ewha.devookserver.service.UserRecommService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
  private final RecommendService recommendService;
  QPost qPost = new QPost("m");
  QPostTag qPostTag = new QPostTag("p");

  public List<Post> searchEngine(List<Post> postList, String question) {
    List<Post> searchResult = new ArrayList<>();

    for (Post post : postList) {

      if (post.getPostTitle().toLowerCase().contains(question.toLowerCase())
          || post.getPostDescription().toLowerCase().contains(question.toLowerCase())) {
        searchResult.add(post);
        continue;
      }

      List<PostTag> postTagList;
      try{
        postTagList = findAllTagsByPost(Integer.parseInt(post.getPostMemo()));
      }catch (Exception e){
        postTagList = findAllTagsByPost(Integer.parseInt(post.getUserIdx()));
      }

      for (PostTag postTag : postTagList) {
        if (postTag.getPostTagName().equalsIgnoreCase(question)) {
          searchResult.add(post);
          break;
        }
      }
    }

    List<Long> exampleResult = new ArrayList<>();
    for (Post post : searchResult) {
      exampleResult.add(post.getPostIdx());
    }

    return searchResult;
  }


  public List<PostTag> findAllTagsByPost(int postIdx) {

    return jpaQueryFactory.selectFrom(qPostTag)
        .where(qPostTag.post_postIdx.eq(postIdx))
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

      List<Post> postList = userBookmarkService.bookmarkExcludeUserPosts(Long.valueOf(userIdx));
      Collections.sort(postList);
      return postList;
    }

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

    if (postRepository.existsByPostIdxAndUserIdx(id, userIdx)) {
      createdAt = postRepository.findByPostIdxAndUserIdx(id, userIdx).getCreatedAt();
    }
    if (userBookmarkRepository.existsUserBookmarkByPostIdxAndUserIdx(id, Long.valueOf(userIdx))) {

      createdAt = userBookmarkRepository.findByPost_postIdxAndUser_userIdx(id,
          Long.valueOf(userIdx)).getCreatedAt();
    }

    if (question == null) {

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

  // ?????? ????????? ??????????????? ??????
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
    return filteredPostList.stream().collect(Collectors.toList());
  }

  public List<Post> tagFiltering2(List<Long> postIdxList, Long id, String userIdx,
      String question) {
    Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());

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
      int limit, String userIdx, Long postIndex) throws JsonProcessingException {

    List<Post> resultList = new ArrayList<>();

    Collections.sort(refrenceDtos);

    for (RefrenceDto refrenceDto : refrenceDtos) {
      if (refrenceDto.getPost().getPostIdx() < id && !Objects.equals(
          refrenceDto.getPost().getUserIdx(),
          userIdx)) {
        resultList.add(refrenceDto.getPost());
      }
    }

    List<Post> testFilteredPostList = recommendService.getRandomPost(postIndex);
    for (Post post : resultList) {
      if (!testFilteredPostList.stream()
          .anyMatch(s -> s.getPostUrl().equals(post.getPostUrl()))
          && !postRepository.existsByPostIdxAndUserIdx(postIndex, userIdx)) {
        testFilteredPostList.add(post);
      }
    }

    return testFilteredPostList.stream().limit(limit).collect(Collectors.toList());
  }

  // ?????? ????????? ??????????????? ??????
  public List<Post> tagFilteringRecommendUser1(List<Long> postIdxList, String userIdx,
      String question, boolean isUser, List<String> requiredTagList, int limit) {

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


  // requiredTagList = ?????? ?????????(?????? ?????? ??????) ?????? -> ????????? '?????????' ????????? ??????! ????????? ?????? ?????????????????? ??????.
  public List<Post> tagFilteringRecommendUser2(List<Long> postIdxList, Long id, String userIdx,
      String question, boolean isUser, List<String> requiredTagList, int limit)
      throws JsonProcessingException {
    Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());

    List<Post> getList = recommendService.getRandom(userIdx);

    List<Post> filteredPostList = new ArrayList<>();
    List<RefrenceDto> resultArray = new ArrayList<>();
    List<String> userPostTag = userRecommService.getPostUserTagList(userIdx);

    int count;

    if (requiredTagList.isEmpty()) {
      for (Post post : getList) {
        count = 0;

        // ?????? post??? tag ?????????
        List<PostTag> eachPostTagList = tagRepository.findAllByPost_postIdx(
            post.getPostIdx().intValue());

        // ???????????? ????????? ????????????,
        // ?????? ???????????? ????????? ?????? post??? ????????? ????????????
        // count ??? ???????????? ???????????? ????????????

        if (!post.getUserIdx().equals(userIdx) && post.getPostIdx() < id) {

          for (PostTag postTag : eachPostTagList) {
            for (String string : userPostTag) {
              if (postTag.getPostTagName().equals(string)) {
                count += 10;
              }
            }
          }

          for (String string : userPostTag) {
            if (post.getPostTitle().contains(string)) {
              count += 3;
            }
            if (post.getPostDescription().contains(string)) {
              count += 1;
            }
          }

          RefrenceDto refrenceDto = new RefrenceDto();
          refrenceDto.setPost(post);
          refrenceDto.setRefrence(count);
          resultArray.add(refrenceDto);

        }
      }

      // Collections.sort(resultArray);

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

    // postIdxList??? Infrastructure ??? ????????? ?????? DB??? post_postIndex ??? ?????????
    // postInxList?????? ?????? ????????? ??????

    List<String> tagListforPostIdexList = new ArrayList<>();
    for (Long postIdx : postIdxList) {
      if (!tagListforPostIdexList.contains(tagRepository.findAllByPost_postIdx(
          Math.toIntExact(postIdx)).get(0).getPostTagName())) {
        tagListforPostIdexList.add(
            tagRepository.findAllByPost_postIdx(Math.toIntExact(postIdx)).get(0).getPostTagName());
      }
    }

    for (Post post : getList) {

      List<PostTag> postTagList = findAllTagsByPost(Integer.parseInt(post.getPostMemo()));

      for (PostTag postTag : postTagList) {
        if (tagListforPostIdexList.contains(postTag.getPostTagName()) && !post.getUserIdx()
            .equals(userIdx) && post.getPostIdx() < id) {
          RefrenceDto refrenceDto = new RefrenceDto();
          refrenceDto.setPost(post);
          refrenceDto.setRefrence(0);
          resultArray.add(refrenceDto);
        }
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

  // ?????? ????????? ??????????????? ??????
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
