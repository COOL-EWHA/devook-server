package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.Notification;
import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.UserBookmark;
import com.ewha.devookserver.dto.post.EachPostResponseDto;
import com.ewha.devookserver.dto.post.PostLabmdaRequestDto;
import com.ewha.devookserver.dto.post.PostLambdaDto;
import com.ewha.devookserver.dto.post.PostUserRequestDto;
import com.ewha.devookserver.dto.post.RequestMemoDto;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import com.ewha.devookserver.service.NotificationService;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.PostService;
import com.ewha.devookserver.service.UserBookmarkService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
public class PostController {

  private final PostService postService;
  private final OauthService oauthService;
  private final PostRepository postRepository;
  private final UserBookmarkRepository userBookmarkRepository;
  private final NotificationService notificationService;
  private final UserBookmarkService userBookmarkService;

  @PostMapping("/bookmarks")
  public ResponseEntity<?> addPost(
      @RequestHeader(value = "Authorization") String tokenGet,
      HttpServletResponse response, @RequestBody PostUserRequestDto postUserRequestDto) {

    try {
      String accessToken = tokenGet.split(" ")[1];
      if (Objects.equals(accessToken, "undefined")) {
        return ResponseEntity.status(401).body("1");
      }

      // 존재하지 않는 유저
      if (!oauthService.isUserExist(accessToken)) {
        return ResponseEntity.status(401).body("2");
      }
      String userIdx = oauthService.getUserIdx(accessToken);

      // 만약 ID 값이 정확히 존재한다면. (null 값으로 판별)
      if (postUserRequestDto.getPostId() != null) {
        Long postIdx = Long.valueOf(postUserRequestDto.getPostId());
        if (postRepository.existsByPostIdx(postIdx)) {
          if (userBookmarkRepository.existsByPost_postIdxAndUser_userIdx(
              Long.valueOf(postUserRequestDto.getPostId()), Long.valueOf(userIdx)) != null) {

            if (postRepository.existsByPostIdxAndUserIdx(
                Long.valueOf(postUserRequestDto.getPostId()),
                userIdx)) {
              return ResponseEntity.status(201).body("DB에는 생성하지 않고, 201 코드 리턴");
            }
            return ResponseEntity.status(201).body("DB에는 생성하지 않고, 201 코드 리턴");
          } else {
            Post post = postRepository.getPostByPostIdx(postIdx);
            post.setUserIdx(userIdx);
            postService.savePostBookmark(Long.valueOf(userIdx), postIdx,
                postUserRequestDto.getMemo());
          }
          return ResponseEntity.status(201).body("북마크 새로 생성완료!");

        }
        // postIdx 에 해당하는 post가 POST table에 존재하지 않을 때
        return ResponseEntity.status(401).body("입력한 postId가 post table에 존재하지 않음");

      }

      if (postService.isPostUserExists(postUserRequestDto.getUrl(), userIdx)) {
        return ResponseEntity.status(201).body("");
      }

      PostLabmdaRequestDto postLabmdaRequestDto = new PostLabmdaRequestDto();
      postLabmdaRequestDto.setUrl(postUserRequestDto.getUrl());

      PostLambdaDto postLambdaDto = postService.getPostInfo(postLabmdaRequestDto);

      postService.savePost(
          postUserRequestDto.getMemo(),
          postUserRequestDto.getUrl(),
          postLambdaDto.getDescription(),
          postLambdaDto.getTitle(),
          postLambdaDto.getImage(), userIdx);

      return ResponseEntity.status(201).body("4");
    } catch (Exception e) {
      return ResponseEntity.status(500).body("5");
    }
  }


  @DeleteMapping("/bookmarks/{bookmarkId}")
  public ResponseEntity<?> deletePost(
      @PathVariable("bookmarkId") int bookmarkId,
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    String accessToken = accessTokenGet.split(" ")[1];

    if (!oauthService.validatieTokenInput(accessToken)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    if (!oauthService.isUserExist(accessToken)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }    // 유저 예외처리 완료
    if (postService.deletePost(bookmarkId, oauthService.getUserIdx(accessToken))) {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    if (userBookmarkService.deleteBookmark(bookmarkId, oauthService.getUserIdx(accessToken))) {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return ResponseEntity.status(401).body("둘다 해당하지 않음");
  }


  @GetMapping("/bookmarks/tags")
  public ResponseEntity<?> getPostsTags(
      @RequestParam(name = "isBookmarkRead", required = false) Boolean isBookmarkRead,
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      return ResponseEntity.status(401).body(" ");
    }
    if (!oauthService.isUserExist(accessToken)) {
      return ResponseEntity.status(401).body(" ");
    }    // 유저 예외처리 완료
    String userIdx = oauthService.getUserIdx(accessToken);
    // 1. userIdx와 일치하는 post

    List<String> finalResponseString = postService.getPostTagList(userIdx, isBookmarkRead);

    // 여기 String 배열 반환
    return ResponseEntity.status(200).body(finalResponseString);


  }


  // 개별 북마크 글 조회 GET
  @GetMapping("/bookmarks/{bookmarkId}")
  public ResponseEntity<?> getPost(
      @PathVariable(name = "bookmarkId") int bookmarkId,
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      return ResponseEntity.status(401).body("1");
    }
    if (!oauthService.isUserExist(accessToken)) {
      return ResponseEntity.status(401).body(" 2");
    }    // 유저 예외처리 완료
    String userIdx = oauthService.getUserIdx(accessToken);

    if (postRepository.existsByPostIdx((long) bookmarkId)) {

      // case 1 postRepository의 해당 userPost가 useridex와 일치할때
      // case 2 그렇지 않을때

      Post userPost = postRepository.getPostByPostIdx((long) bookmarkId);

      // case1
      if (userPost.getUserIdx().equals(userIdx)) {

        List<String> tagList = postService.getEachPostTagList(bookmarkId);
        if (tagList.size() == 0) {
          tagList.add("태그1");
          tagList.add("태그2");
        }

        SimpleDateFormat formatISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        Date dBconvertedTime = userPost.getCreatedAt();
        String dBCreatedAt = formatISO.format(dBconvertedTime);

        Notification notification = notificationService.returnDueDate((long) bookmarkId,
            Long.valueOf(userIdx), true);

        String convertedDueDate = null;
        if (notification != null) {
          if (notification.getDueDate() == null) {
            convertedDueDate = null;
          } else {
            convertedDueDate = notification.getDueDate().format(
                DateTimeFormatter.ofPattern("yyyy.MM.dd"));
          }
        }

        EachPostResponseDto eachPostResponseDto = EachPostResponseDto.builder()
            .id(userPost.getId())
            .title(userPost.getPostTitle())
            .thumbnail(userPost.getPostThumbnail())
            .description(userPost.getPostDescription())
            .tags(tagList)
            .url(userPost.getPostUrl())
            .createdAt(dBCreatedAt)
            .memo(userPost.getPostMemo())
            .isRead(userPost.getIsRead()) //isRead, dueDate 추가
            .dueDate(convertedDueDate)
            .build();

        return ResponseEntity.status(200).body(eachPostResponseDto);
      } else { //case2

        UserBookmark userBookmark = userBookmarkRepository.findByPost_postIdxAndUser_userIdx(
            userPost.getPostIdx(), Long.valueOf(userIdx));

        List<String> tagList = postService.getEachPostTagList(bookmarkId);
        if (tagList.size() == 0) {
          tagList.add("태그1");
          tagList.add("태그2");
        }

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        Date dBconvertedTime = userBookmark.getCreatedAt();
        String dBCreatedAt = formatISO.format(dBconvertedTime);

        Notification notification = notificationService.returnDueDate((long) bookmarkId,
            Long.valueOf(userIdx), true);

        String convertedDueDate = null;
        if (notification != null) {
          if (notification.getDueDate() == null) {
            convertedDueDate = null;
          } else {
            convertedDueDate = notification.getDueDate().format(
                DateTimeFormatter.ofPattern("yyyy.MM.dd"));
          }
        }

        EachPostResponseDto eachPostResponseDto = EachPostResponseDto.builder()
            .id(userPost.getId())
            .title(userPost.getPostTitle())
            .thumbnail(userPost.getPostThumbnail())
            .thumbnail(userPost.getPostThumbnail())
            .description(userPost.getPostDescription())
            .tags(tagList)
            .url(userPost.getPostUrl())
            .createdAt(dBCreatedAt)
            .memo(userBookmark.getMemo())
            .isRead(userBookmark.getIsRead())
            .dueDate(convertedDueDate)
            .build();

        return ResponseEntity.status(200).body(eachPostResponseDto);


      }

    }
    return ResponseEntity.status(404).body("3");


  }

  @PatchMapping("/bookmarks/{bookmarkId}")
  public ResponseEntity<?> editPost(
      @PathVariable(name = "bookmarkId") int bookmarkId,
      @RequestHeader(name = "Authorization") String accessTokenGet,
      @RequestBody RequestMemoDto requestMemoDto) throws ParseException {

    String requestMemo = requestMemoDto.getMemo();
    String dueDateGet = requestMemoDto.getDueDate();
    Boolean isRead = requestMemoDto.getIsRead();

    LocalDate dueDate = null;

    // 빈 문자열로 들어올 경우 해당 dueDate null 값으로 설정

    try {
      String accessToken = accessTokenGet.split(" ")[1];
      if (!oauthService.validatieTokenInput(accessToken)) {
        return ResponseEntity.status(401).body("1");
      }
      if (!oauthService.isUserExist(accessToken)) {
        return ResponseEntity.status(401).body(" 2");
      }    // 유저 예외처리 완료

      String userIdx = oauthService.getUserIdx(accessToken);

      if (dueDateGet == "") {
        notificationService.deleteDueDate((long) bookmarkId, Long.valueOf(userIdx));
      }
      if (dueDateGet != null && dueDateGet != "") {
        String dueDateSplittedYear = dueDateGet.split("\\.")[0];
        String dueDateSplittedMonth = dueDateGet.split("\\.")[1];
        String dueDateSplittedDate = dueDateGet.split("\\.")[2];

        String dueDateIntegration = dueDateSplittedYear.concat("-")
            .concat(dueDateSplittedMonth)
            .concat("-")
            .concat(dueDateSplittedDate);

        dueDate = LocalDate.parse(dueDateIntegration, DateTimeFormatter.ISO_LOCAL_DATE);
      }

      if (isRead == null) {
        isRead = false;
      }

      if (postRepository.existsByPostIdx((long) bookmarkId)) {
        if (postRepository.getPostByPostIdx(Long.valueOf(bookmarkId)).getUserIdx()
            .equals(userIdx)) {

          Post newPost = postRepository.getPostByPostIdx(Long.valueOf(bookmarkId));

          if (requestMemo != null) {
            newPost.setPostMemo(requestMemo);
          }
          if (isRead != null) {
            newPost.setIsRead(isRead);
          }

          // notification 에도 저장

          postRepository.save(newPost);

          notificationService.saveNotification((long) bookmarkId, Long.valueOf(userIdx), true,
              dueDate);

          return ResponseEntity.status(200).body(" ");
        }
      }

      if (userBookmarkRepository.findByPost_postIdxAndUser_userIdx((long) bookmarkId,
          Long.valueOf(userIdx)) != null) {

        UserBookmark userBookmark = userBookmarkRepository.findByPost_postIdxAndUser_userIdx(
            (long) bookmarkId, Long.valueOf(userIdx));

        if (requestMemo != null) {
          userBookmark.setMemo(requestMemo);
        }
        if (isRead != null) {
          userBookmark.setIsRead(isRead);
        }

        userBookmarkRepository.save(userBookmark);
        notificationService.saveNotification((long) bookmarkId, Long.valueOf(userIdx), false,
            dueDate);

        return ResponseEntity.status(200).body(" ");

      }

      return ResponseEntity.status(401).body(" 3");


    } catch (Exception e) {
      return ResponseEntity.status(401).body(" 4");
    }

  }
}
