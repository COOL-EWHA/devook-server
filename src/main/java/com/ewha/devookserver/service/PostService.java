package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.post.CursorResult;
import com.ewha.devookserver.domain.post.Notification;
import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.UserBookmark;
import com.ewha.devookserver.dto.post.CrawlerReqeustDto;
import com.ewha.devookserver.dto.post.CrawlerResponseDto;
import com.ewha.devookserver.dto.post.PostAddResponseDto;
import com.ewha.devookserver.dto.post.PostBookmarkRequestDto;
import com.ewha.devookserver.dto.post.PostLabmdaRequestDto;
import com.ewha.devookserver.dto.post.PostLambdaDto;
import com.ewha.devookserver.dto.post.PostListDto;
import com.ewha.devookserver.repository.MemberRepository;
import com.ewha.devookserver.repository.NotificationRepository;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.QueryRepository;
import com.ewha.devookserver.repository.TagRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
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
  private final TagRepository tagRepository;
  WebClient webClient = WebClient.create(
      "https://sy54a2wnyl.execute-api.ap-northeast-2.amazonaws.com/test");
  WebClient crawlerClient = WebClient.create(
      "http://52.79.251.102");

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

  // TODO 유저에게 리턴해주는 posts 들의 tags를 리턴해줘야 함.
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

  public List<PostListDto> responseListMaker(CursorResult<Post> productList, Boolean isRead,
      Long userIdx) {
    List<PostListDto> searchResponseDtoList = new ArrayList<>();
    List<PostListDto> isReadReturnDtoList = new ArrayList<>();

    for (Post post : productList.getValues()) {
      List<String> forTestString = new ArrayList<>();
      List<PostTag> postTagList = queryRepository.findAllTagsByPost(post.getPostIdx().intValue());

      Notification notification = notificationService.returnDueDate(post.getPostIdx(),
          userIdx, true);

      Boolean isUserPost = postRepository.existsByPostIdxAndUserIdx(post.getPostIdx(),
          String.valueOf(userIdx));

      Boolean isReadPost;
      if (isUserPost == false) {
        UserBookmark userBookmark = userBookmarkRepository.findByPost_postIdxAndUser_userIdx(
            post.getPostIdx(), userIdx);
        isReadPost = userBookmark.getIsRead();
      } else {
        isReadPost = post.getIsRead();
      }

      String convertedDueDate = null;
      if (notification != null) {
        if (notification.getDueDate() == null) {
          convertedDueDate = null;
        } else {
          convertedDueDate = notification.getDueDate().format(
              DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        }
      }

      for (PostTag postTag : postTagList) {
        forTestString.add(postTag.getPostTagName());
      }

      /*
      if (forTestString.size() == 0) {
        forTestString.add("태그1");
        forTestString.add("태그2");
      }

       */

      PostListDto postListDto = PostListDto.builder()
          .id(post.getId())
          .thumbnail(post.getPostThumbnail())
          .description(post.getPostDescription())
          .title(post.getPostTitle())
          .tags(forTestString)
          .url(post.getPostUrl())
          .isRead(isReadPost)
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

      /*
      if (forTestString.size() == 0) {
        forTestString.add("태그1");
        forTestString.add("태그2");
      }

       */

      // userIdx는 고정값이 아니기 때문에 controller에서 직접 받아와야 한다.

      boolean getIsBookmarked = recommendService.checkIsBookmarked(post.getId(), userIdx);

      if (forTestString.size() != 0 && getIsBookmarked == false) {
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

    }
    return searchResponseDtoList;
  }

  public List<PostBookmarkRequestDto> responseBookmarkListMakerForPost(
      CursorResult<Post> productList,
      String userIdx) {
    List<PostBookmarkRequestDto> searchResponseDtoList = new ArrayList<>();

    for (Post post : productList.getValues()) {
      List<String> forTestString = new ArrayList<>();
      List<PostTag> postTagList = queryRepository.findAllTagsByPost(post.getPostIdx().intValue());

      for (PostTag postTag : postTagList) {
        forTestString.add(postTag.getPostTagName());
      }
  /*
      if (forTestString.size() == 0) {
        forTestString.add("태그1");
        forTestString.add("태그2");
      }


   */
      // userIdx는 고정값이 아니기 때문에 controller에서 직접 받아와야 한다.
      boolean getIsBookmarked = recommendService.checkIsBookmarked(post.getId(), userIdx);
      if (forTestString.size() != 0 && getIsBookmarked == false) {
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

      if (forTestString.size() != 0) {
        PostBookmarkRequestDto postListDto = PostBookmarkRequestDto.builder()
            .id(post.getId())
            .thumbnail(post.getPostThumbnail())
            .description(post.getPostDescription())
            .title(post.getPostTitle())
            .tags(forTestString)
            .url(post.getPostUrl())
            .isBookmarked(null) // 여기 수정! (일단 기본값으로)
            .value(Long.valueOf(post.getPostMemo()))
            .build();
        searchResponseDtoList.add(postListDto);
      }
    }

    return searchResponseDtoList;
  }


  // @Postmapping 요청에 대한 responseBody maker
  public PostAddResponseDto postAddBodyMaker(Long id, String title, String thumbnail,
      String description, Boolean isRead, String url, String dueDate, List<String> tags) {

    PostAddResponseDto postAddResponseDto = PostAddResponseDto.builder()
        .id(id)
        .title(title)
        .thumbnail(thumbnail)
        .description(description)
        .isRead(isRead)
        .url(url)
        .dueDate(dueDate)
        .tags(tags)
        .build();

    return postAddResponseDto;
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
        userBookmarkRepository.deleteAllByPostIdx((long) postIdx);
        postRepository.deletePostByPostIdx((long) postIdx);
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  public void savePost(String memo, String url, String description, String title, String image,
      String userIdx) throws InterruptedException, JsonProcessingException {

    Post post = Post.builder()
        .postMemo(memo)
        .postUrl(url)
        .postDescription(description)
        .postThumbnail(image)
        .postTitle(title)
        .userIdx(userIdx)
        .build();

    postRepository.save(post);
    Post savedPost = postRepository.getPostByPostUrlAndUserIdx(post.getPostUrl(),
        post.getUserIdx());

    // post 카테고리 저장
    CrawlerReqeustDto crawlerReqeustDto = new CrawlerReqeustDto();
    crawlerReqeustDto.setTitle(post.getPostTitle());

    PostTag postTag = PostTag.builder()
        .postTagName(getPostCategory(crawlerReqeustDto))
        .post_postIdx(Math.toIntExact(savedPost.getPostIdx()))
        .build();

    tagRepository.save(postTag);
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

  public String getPostCategory(CrawlerReqeustDto crawlerReqeustDto)
      throws JsonProcessingException, InterruptedException {

    JsonNode result = crawlerClient.post()
        .uri("/category")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(Mono.just(crawlerReqeustDto), CrawlerReqeustDto.class)
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
      CrawlerResponseDto crawlerResponseDto = objectMapper.readValue(returnValue,
          CrawlerResponseDto.class);
      return crawlerResponseDto.getCategory();
    }
    return null;
  }

}
