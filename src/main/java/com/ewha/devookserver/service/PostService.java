package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.CursorResult;
import com.ewha.devookserver.domain.post.Notification;
import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.UserBookmark;
import com.ewha.devookserver.dto.post.PostBookmarkRequestDto;
import com.ewha.devookserver.dto.post.PostLabmdaRequestDto;
import com.ewha.devookserver.dto.post.PostLambdaDto;
import com.ewha.devookserver.dto.post.PostListDto;
import com.ewha.devookserver.repository.MemberRepository;
import com.ewha.devookserver.repository.NotificationRepository;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.QueryRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class PostService {

  private final MemberRepository memberRepository;
  private final PostRepository postRepository;
  private final QueryRepository queryRepository;
  private final UserBookmarkRepository userBookmarkRepository;
  private final RecommendService recommendService;
  private final NotificationRepository notificationRepository;
  private final NotificationService notificationService;
  WebClient webClient = WebClient.create(
      "https://sy54a2wnyl.execute-api.ap-northeast-2.amazonaws.com/test");

  public boolean isPostUserExists(String url, String userIdx) {
    return postRepository.getPostByPostUrlAndUserIdx(url, userIdx) != null;
  }

  public List<Post> getTestPage() {
    return postRepository.findWithPagination(Pageable.ofSize(5));
  }

  public List<String> getEachPostTagList(int id) {
    List<PostTag> postTagList = queryRepository.findAllTagsByPost(id);
    List<String> searchResponseDtoList = new ArrayList<>();

    for (PostTag postTag : postTagList) {
      searchResponseDtoList.add(postTag.getPostTagName());
    }

    return searchResponseDtoList;
  }

  public List<String> getPostTagList(String userIdx, Boolean isBookmarkRead) {

    List<Post> isBookmarkReadList = new ArrayList<>();

    List<Post> returnPost = postRepository.findAllByUserIdx(userIdx);

    List<UserBookmark> addBookmark = userBookmarkRepository.findAllByUser_userIdx(
        Long.valueOf(userIdx));

    for (UserBookmark userBookmark : addBookmark) {
      returnPost.add(postRepository.getPostByPostIdx(userBookmark.getPostIdx())
      );
    }

    // isBookmarkRead 고려

    if (isBookmarkRead == null) {

      for (Post post : returnPost) {
        isBookmarkReadList.add(post);
      }
    } else {

      for (Post post : returnPost) {
        if (post.getIsRead().booleanValue() == isBookmarkRead) {
          isBookmarkReadList.add(post);
        }
      }
    }

    List<String> searchResponseDtoList = new ArrayList<>();

    for (Post post : isBookmarkReadList) {
      List<PostTag> postTagList = queryRepository.findAllTagsByPost(post.getPostIdx().intValue());

      for (PostTag postTag : postTagList) {
        if (!searchResponseDtoList.contains(postTag.getPostTagName())) {
          searchResponseDtoList.add(postTag.getPostTagName());
        }
      }
    }

    return searchResponseDtoList;
  }

  public List<String> getPostTagList(String userIdx) {
    List<Post> returnPost = postRepository.findAllByUserIdx(userIdx);

    List<UserBookmark> addBookmark = userBookmarkRepository.findAllByUser_userIdx(
        Long.valueOf(userIdx));

    for (UserBookmark userBookmark : addBookmark) {
      returnPost.add(postRepository.getPostByPostIdx(userBookmark.getPostIdx())
      );
    }

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

  public List<String> getPostTagList() {
    List<Post> returnPost = postRepository.findAll();

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

  public List<PostListDto> responseListMaker(CursorResult<Post> productList, Boolean isRead) {
    List<PostListDto> searchResponseDtoList = new ArrayList<>();
    List<PostListDto> isReadReturnDtoList = new ArrayList<>();

    for (Post post : productList.getValues()) {
      List<String> forTestString = new ArrayList<>();
      List<PostTag> postTagList = queryRepository.findAllTagsByPost(post.getPostIdx().intValue());

      Notification notification = notificationService.returnDueDate(post.getPostIdx(),
          Long.valueOf(post.getUserIdx()), true);

      SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");

      String convertedDueDate = null;
      if (notification != null) {
        if (notification.getDueDate() == null) {
          convertedDueDate = null;
        } else {
          convertedDueDate = format2.format(notification.getDueDate());
        }
      }

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
          .isRead(post.getIsRead())
          .dueDate(convertedDueDate)
          .build();
      searchResponseDtoList.add(postListDto);
    }
    if (isRead == null) {
      return searchResponseDtoList.stream().limit(10).collect(Collectors.toList());
    }
    for (PostListDto postListDto : searchResponseDtoList) {
      if (postListDto.getIsRead().booleanValue() == isRead) {
        isReadReturnDtoList.add(postListDto);
      }
    }

    return isReadReturnDtoList.stream().limit(10).collect(Collectors.toList());
  }

  public List<PostBookmarkRequestDto> responseBookmarkListMaker(CursorResult<Post> productList,
      String userIdx) {
    List<PostBookmarkRequestDto> searchResponseDtoList = new ArrayList<>();

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

      // userIdx는 고정값이 아니기 때문에 controller에서 직접 받아와야 한다.
      boolean getIsBookmarked = recommendService.checkIsBookmarked(post.getId(), userIdx);
      System.out.println(getIsBookmarked);

      PostBookmarkRequestDto postListDto = PostBookmarkRequestDto.builder()
          .id(post.getId())
          .thumbnail(post.getPostThumbnail())
          .description(post.getPostDescription())
          .title(post.getPostTitle())
          .tags(forTestString)
          .url(post.getPostUrl())
          .isBookmarked(getIsBookmarked) // 여기 수정! (일단 기본값으로)
          .build();
      searchResponseDtoList.add(postListDto);
    }
    return searchResponseDtoList;
  }

  public List<PostBookmarkRequestDto> responseBookmarkListMakerForNoAuthUser(
      CursorResult<Post> productList) {
    List<PostBookmarkRequestDto> searchResponseDtoList = new ArrayList<>();

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

      PostBookmarkRequestDto postListDto = PostBookmarkRequestDto.builder()
          .id(post.getId())
          .thumbnail(post.getPostThumbnail())
          .description(post.getPostDescription())
          .title(post.getPostTitle())
          .tags(forTestString)
          .url(post.getPostUrl())
          .isBookmarked(null) // 여기 수정! (일단 기본값으로)
          .build();
      searchResponseDtoList.add(postListDto);
    }
    return searchResponseDtoList;
  }

  public CursorResult<Post> get(Long cursorId, Pageable page, String userIdx, String question) {
    final List<Post> boards = getPost(cursorId, page, userIdx, question);
    final Long lastIdofList = boards.isEmpty() ?
        null : boards.get(boards.size() - 1).getId();

    return new CursorResult<>(boards, hasNext(lastIdofList));
  }

  public List<Post> getPost(Long id, Pageable page, String userIdx, String question) {
    return id == null ?
        postRepository.findAllByPostIdx(page, userIdx) :
        postRepository.findAllByPostIdxDesc(id, page, userIdx);
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

  public void savePostBookmark(Long user_userIdx, Long post_postIdx, String memo) {

    if (postRepository.existsByPostIdxAndUserIdx(post_postIdx, String.valueOf(user_userIdx))) {
      Post post = postRepository.findByPostIdxAndUserIdx(post_postIdx,
          String.valueOf(user_userIdx));
      Timestamp timestamp = new Timestamp(System.currentTimeMillis());
      post.setCreatedAt(timestamp);
      postRepository.save(post);
      return;
    }

    UserBookmark userBookmark = UserBookmark.builder()
        .postIdx(post_postIdx)
        .userIdx(user_userIdx)
        .memo(memo)
        .build();

    userBookmarkRepository.save(userBookmark);
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
