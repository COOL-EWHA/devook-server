package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.CursorResult;
import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.RefrenceDto;
import com.ewha.devookserver.dto.post.PostLabmdaRequestDto;
import com.ewha.devookserver.dto.post.PostLambdaDto;
import com.ewha.devookserver.dto.post.PostListDto;
import com.ewha.devookserver.repository.MemberRepository;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.QueryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class QueryService {

  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final QueryRepository queryRepository;
  WebClient webClient = WebClient.create(
      "https://sy54a2wnyl.execute-api.ap-northeast-2.amazonaws.com/test");

  public boolean isPostUserExists(String url, String userIdx) {
    return postRepository.getPostByPostUrlAndUserIdx(url, userIdx) != null;
  }

  public List<Post> getTestPage() {
    return postRepository.findWithPagination(Pageable.ofSize(5));
  }

  public List<String> getPostTagList(String userIdx) {
    List<Post> returnPost = postRepository.findAllByUserIdx(userIdx);

    List<String> searchResponseDtoList = new ArrayList<>();

    for (Post post : returnPost) {
      List<PostTag> postTagList = queryRepository.findAllTagsByPost(post.getPostIdx().intValue());

      for (PostTag postTag : postTagList) {
        if (!searchResponseDtoList.contains(postTag.getPostTagName())) {
          searchResponseDtoList.add(postTag.getPostTagName());
        }
      }
    }

    return searchResponseDtoList;

  }

  public List<PostListDto> responseListMaker(CursorResult<Post> productList) {
    List<PostListDto> searchResponseDtoList = new ArrayList<>();

    for (Post post : productList.getValues()) {
      List<String> forTestString = new ArrayList<>();
      List<PostTag> postTagList = queryRepository.findAllTagsByPost(post.getPostIdx().intValue());

      for (PostTag postTag : postTagList) {
        forTestString.add(postTag.getPostTagName());
      }

      if (forTestString.size() == 0) {
        forTestString.add("태그1");
        forTestString.add("태그2");
      }

      PostListDto postListDto = PostListDto.builder()
          .id(post.getId())
          .thumbnail(post.getPostThumbnail())
          .description(post.getPostDescription())
          .title(post.getPostTitle())
          .tags(forTestString)
          .url(post.getPostUrl())
          .build();
      searchResponseDtoList.add(postListDto);
    }
    return searchResponseDtoList;
  }

  // 11.21 @ 1:06:05 추가사항

  // 현재 새로 짠 함수 전용 get, getPost

  public CursorResult<Post> get(Long cursorId, Pageable page, String userIdx, String question) {
    final List<Post> boards = getPost(cursorId, page, userIdx, question);
    final Long lastIdofList = boards.isEmpty() ?
        null : boards.get(boards.size() - 1).getId();

    return new CursorResult<>(boards, hasNext(lastIdofList));
  }

  public CursorResult<Post> get(Long cursorId, Pageable page, String userIdx, String question,
      List<Long> postTaglist) {
    final List<Post> boards = getPost(cursorId, page, userIdx, question, postTaglist);
    final Long lastIdofList = boards.isEmpty() ?
        null : boards.get(boards.size() - 1).getId();

    return new CursorResult<>(boards, hasNext(lastIdofList));
  }

  public CursorResult<Post> get(Long cursorId, Pageable page, String userIdx, String question,
      List<Long> postTaglist, boolean isRecommend, List<String> postTagList, int limit) {
    final List<Post> boards = getPost(cursorId, page, userIdx, question, postTaglist, isRecommend,
        postTagList, limit);
    final Long lastIdofList = boards.isEmpty() ?
        null : boards.get(boards.size() - 1).getId();

    return new CursorResult<>(boards, hasNext(lastIdofList));
  }

  // 1.25 05:59 추가
  public CursorResult<Post> get(Long cursorId, Pageable page, List<RefrenceDto> requestDtoList,
      int limit, String userIdx) {
    final List<Post> boards = getPost(cursorId, page, requestDtoList, limit, userIdx);
    final Long lastIdofList = boards.isEmpty() ?
        null : boards.get(boards.size() - 1).getId();

    return new CursorResult<>(boards, hasNext(lastIdofList));
  }

  public List<Post> getPost(Long id, Pageable page, String userIdx, String question,
      List<Long> postTagList) {
    return id == null ?
        queryRepository.tagFiltering(postTagList, userIdx, question) :
        queryRepository.tagFiltering2(postTagList, id, userIdx, question);
  }

  public List<Post> getPost(Long id, Pageable page, String userIdx, String question) {
    return id == null ?
        queryRepository.findAllByPostIdxFunction1(page, userIdx, question) :
        queryRepository.findAllByPostIdxDescFunction2(id, page, userIdx, question);
  }

  public List<Post> getPost(Long id, Pageable page, List<RefrenceDto> requestDtoList, int limit,
      String userIdx) {
    return id == null ?
        queryRepository.recommendPost1(page, requestDtoList, limit, userIdx) :
        queryRepository.recommendPost2(id, page, requestDtoList, limit, userIdx);
  }

  public List<Post> getPost(Long id, Pageable page, String userIdx, String question,
      List<Long> postTagList, boolean isRecommend, List<String> requiredList, int limit) {
    return id == null ?
        queryRepository.tagFilteringRecommendUser1(postTagList, userIdx, question, isRecommend,
            requiredList, limit) :
        queryRepository.tagFilteringRecommendUser2(postTagList, id, userIdx, question, isRecommend,
            requiredList, limit);
  }

  public Boolean hasNext(Long id) {
    if (id == null) {
      return false;
    }
    return this.postRepository.existsByPostIdx(id);
  }

  public boolean deletePost(int postIdx, String userIdx) {
    if (postRepository.existsByPostIdx((long) postIdx)) {
      if (Objects.equals(postRepository.getPostByPostIdx((long) postIdx).getUserIdx(), userIdx)) {
        postRepository.deletePostByPostIdx((long) postIdx);
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  public void savePost(String memo, String url, String description, String title, String image,
      String userIdx) {

    Post post = Post.builder()
        .postMemo(memo)
        .postUrl(url)
        .postDescription(description)
        .postThumbnail(image)
        .postTitle(title)
        .userIdx(userIdx)
        .build();

    postRepository.save(post);
  }

  public boolean isPostExists(String url) {
    return postRepository.existsByPostUrl(url);
  }


  // lambda 함수 읽어오는 부분 ( web-crawler ) WebClient 사용
  public PostLambdaDto getPostInfo(PostLabmdaRequestDto postLabmdaRequestDto)
      throws JsonProcessingException {

    JsonNode result = webClient.post()
        .uri("/test")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(Mono.just(postLabmdaRequestDto), PostLabmdaRequestDto.class)
        .retrieve()
        .bodyToMono(String.class).map(s -> {
          ObjectMapper mapper = new ObjectMapper();
          try {
            return mapper.readTree(s);
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }
          return null;
        })
        .block();

    System.out.println(result);

    ObjectMapper objectMapper = new ObjectMapper();
    if (result != null) {
      String returnValue = objectMapper.writeValueAsString(result);
      PostLambdaDto postLambdaDto = objectMapper.readValue(returnValue, PostLambdaDto.class);
      return postLambdaDto;
    }
    return null;
  }
}
