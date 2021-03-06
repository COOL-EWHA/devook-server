package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.post.Notification;
import com.ewha.devookserver.domain.post.Post;
import com.ewha.devookserver.domain.post.PostTag;
import com.ewha.devookserver.domain.post.UserBookmark;
import com.ewha.devookserver.dto.post.EachPostResponseDto;
import com.ewha.devookserver.dto.post.PostAddResponseDto;
import com.ewha.devookserver.dto.post.PostLabmdaRequestDto;
import com.ewha.devookserver.dto.post.PostLambdaDto;
import com.ewha.devookserver.dto.post.PostUserRequestDto;
import com.ewha.devookserver.dto.post.RequestMemoDto;
import com.ewha.devookserver.repository.PostRepository;
import com.ewha.devookserver.repository.TagRepository;
import com.ewha.devookserver.repository.UserBookmarkRepository;
import com.ewha.devookserver.service.CrawlerService;
import com.ewha.devookserver.service.NotificationService;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.PostService;
import com.ewha.devookserver.service.UserBookmarkService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
  private final CrawlerService crawlerService;
  private final UserBookmarkRepository userBookmarkRepository;
  private final NotificationService notificationService;
  private final UserBookmarkService userBookmarkService;
  private final TagRepository tagRepository;

  @GetMapping("/crawler")
  public void addCrawlerResult() throws JsonProcessingException, InterruptedException {
    crawlerService.getAICrawler();
  }

  @PostMapping("/bookmarks")
  public ResponseEntity<?> addPost(
      @RequestHeader(value = "Authorization") String tokenGet,
      HttpServletResponse response, @RequestBody PostUserRequestDto postUserRequestDto) {

    try {
      String accessToken = tokenGet.split(" ")[1];
      List<String> forTestString = new ArrayList<>();

      if (Objects.equals(accessToken, "undefined")) {
        return ResponseEntity.status(401).body("1");
      }

      if (!oauthService.isUserExist(accessToken)) {
        return ResponseEntity.status(401).body("2");
      }
      String userIdx = oauthService.getUserIdx(accessToken);

      if (postUserRequestDto.getPostId() != null) {
        Long postIdx = Long.valueOf(postUserRequestDto.getPostId());
        if (postRepository.existsByPostIdx(postIdx)) {
          if (userBookmarkRepository.existsByPost_postIdxAndUser_userIdx(
              Long.valueOf(postUserRequestDto.getPostId()), Long.valueOf(userIdx)) != null) {

            Post post = postRepository.getPostByPostIdx(
                Long.valueOf(postUserRequestDto.getPostId()));

            List<PostTag> allTags = tagRepository.findAllByPost_postIdx(
                Math.toIntExact(post.getPostIdx()));

            for (PostTag postTag : allTags) {
              forTestString.add(postTag.getPostTagName());
            }

            PostAddResponseDto postAddResponseDto = postService.postAddBodyMaker(
                post.getPostIdx(),
                post.getPostTitle(),
                post.getPostThumbnail(),
                post.getPostDescription(),
                false,
                post.getPostUrl(),
                null,
                forTestString
            );
            return ResponseEntity.status(201).body(postAddResponseDto);
          } else {
            Post post = postRepository.getPostByPostIdx(postIdx);

            List<PostTag> allTags = tagRepository.findAllByPost_postIdx(
                Math.toIntExact(post.getPostIdx()));

            for (PostTag postTag : allTags) {
              forTestString.add(postTag.getPostTagName());
            }
            post.setUserIdx(userIdx);
            postService.savePostBookmark(Long.valueOf(userIdx), postIdx,
                postUserRequestDto.getMemo());
            PostAddResponseDto postAddResponseDto = postService.postAddBodyMaker(
                post.getPostIdx(),
                post.getPostTitle(),
                post.getPostThumbnail(),
                post.getPostDescription(),
                false,
                post.getPostUrl(),
                null,
                forTestString
            );
            return ResponseEntity.status(201).body(postAddResponseDto);
          }
        }
        return ResponseEntity.status(401).body("????????? postId??? post table??? ???????????? ??????");
      }
      if (postService.isPostUserExists(postUserRequestDto.getUrl(), userIdx)) {
        Post post = postRepository.getPostByPostUrlAndUserIdx(postUserRequestDto.getUrl(), userIdx);

        List<PostTag> allTags = tagRepository.findAllByPost_postIdx(
            Math.toIntExact(post.getPostIdx()));

        for (PostTag postTag : allTags) {
          forTestString.add(postTag.getPostTagName());
        }
        post.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        postRepository.save(post);

        PostAddResponseDto postAddResponseDto = postService.postAddBodyMaker(
            post.getPostIdx(),
            post.getPostTitle(),
            post.getPostThumbnail(),
            post.getPostDescription(),
            post.getIsRead(),
            post.getPostUrl(),
            null,
            forTestString
        );

        return ResponseEntity.status(201).body(postAddResponseDto);
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

      Post post = postRepository.getPostByPostUrlAndUserIdx(postUserRequestDto.getUrl(), userIdx);
      List<PostTag> allTags = tagRepository.findAllByPost_postIdx(
          Math.toIntExact(post.getPostIdx()));

      for (PostTag postTag : allTags) {
        forTestString.add(postTag.getPostTagName());
      }
      PostAddResponseDto postAddResponseDto = postService.postAddBodyMaker(
          post.getPostIdx(),
          postLambdaDto.getTitle(),
          postLambdaDto.getImage(),
          postLambdaDto.getDescription(),
          false,
          postUserRequestDto.getUrl(),
          null,
          forTestString
      );

      return ResponseEntity.status(201).body(postAddResponseDto);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("????????? ??????, ????????? ?????? ??????");
    }
  }


  @DeleteMapping("/bookmarks/{bookmarkId}")
  public ResponseEntity<String> deletePost(
      @PathVariable("bookmarkId") int bookmarkId,
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    String accessToken = accessTokenGet.split(" ")[1];

    if (!oauthService.validatieTokenInput(accessToken)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    if (!oauthService.isUserExist(accessToken)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }    // ?????? ???????????? ??????
    if (postService.deletePost(bookmarkId, oauthService.getUserIdx(accessToken))) {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    if (userBookmarkService.deleteBookmark(bookmarkId, oauthService.getUserIdx(accessToken))) {
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return ResponseEntity.status(401).body("?????? ???????????? ??????");
  }


  @GetMapping("/bookmarks/tags")
  public ResponseEntity<List<String>> getPostsTags(
      @RequestParam(name = "isBookmarkRead", required = false) Boolean isBookmarkRead,
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    if (!oauthService.isUserExist(accessToken)) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }    // ?????? ???????????? ??????
    String userIdx = oauthService.getUserIdx(accessToken);
    // 1. userIdx??? ???????????? post

    List<String> finalResponseString = postService.getPostTagList(userIdx, isBookmarkRead);
    Collections.sort(finalResponseString);
    // ?????? String ?????? ??????
    return ResponseEntity.status(200).body(finalResponseString);


  }


  // ?????? ????????? ??? ?????? GET
  @GetMapping("/bookmarks/{bookmarkId}")
  public ResponseEntity<EachPostResponseDto> getPost(
      @PathVariable(name = "bookmarkId") int bookmarkId,
      @RequestHeader(name = "Authorization") String accessTokenGet) {

    String accessToken = accessTokenGet.split(" ")[1];
    if (!oauthService.validatieTokenInput(accessToken)) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    if (!oauthService.isUserExist(accessToken)) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    String userIdx = oauthService.getUserIdx(accessToken);

    if (postRepository.existsByPostIdx((long) bookmarkId)) {

      Post userPost = postRepository.getPostByPostIdx((long) bookmarkId);

      if (userPost.getUserIdx().equals(userIdx)) {

        List<String> tagList = postService.getEachPostTagList(bookmarkId);

        SimpleDateFormat formatISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        Calendar cal = Calendar.getInstance();
        cal.setTime(userPost.getCreatedAt());
        cal.add(Calendar.HOUR, -9);

        Date dBconvertedTime = cal.getTime();
        String dBCreatedAt = formatISO.format(dBconvertedTime);

        Notification notification = notificationService.returnDueDate((long) bookmarkId,
            Long.valueOf(userIdx), true);

        String convertedDueDate = null;
        if (notification != null) {
          if (notification.getDueDate() == null) {
            convertedDueDate = null;
          } else {
            convertedDueDate = notification.getDueDate().format(
                DateTimeFormatter.ofPattern("yyyy/MM/dd"));
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
            .isRead(userPost.getIsRead())
            .dueDate(convertedDueDate)
            .build();

        return ResponseEntity.status(200).body(eachPostResponseDto);
      } else {

        UserBookmark userBookmark = userBookmarkRepository.findByPost_postIdxAndUser_userIdx(
            userPost.getPostIdx(), Long.valueOf(userIdx));

        List<String> tagList = postService.getEachPostTagList(bookmarkId);

        SimpleDateFormat formatISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Calendar cal = Calendar.getInstance();
        cal.setTime(userBookmark.getCreatedAt());
        cal.add(Calendar.HOUR, -9);

        Date dBconvertedTime = cal.getTime();
        String dBCreatedAt = formatISO.format(dBconvertedTime);

        Notification notification = notificationService.returnDueDate((long) bookmarkId,
            Long.valueOf(userIdx), true);

        String convertedDueDate = null;
        if (notification != null) {
          if (notification.getDueDate() == null) {
            convertedDueDate = null;
          } else {
            convertedDueDate = notification.getDueDate().format(
                DateTimeFormatter.ofPattern("yyyy/MM/dd"));
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
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);


  }

  @PatchMapping("/bookmarks/{bookmarkId}")
  public ResponseEntity<String> editPost(
      @PathVariable(name = "bookmarkId") int bookmarkId,
      @RequestHeader(name = "Authorization") String accessTokenGet,
      @RequestBody RequestMemoDto requestMemoDto) {

    String requestMemo = requestMemoDto.getMemo();
    String dueDateGet = requestMemoDto.getDueDate();
    Boolean isRead = requestMemoDto.getIsRead();

    LocalDate dueDate = null;

    // ??? ???????????? ????????? ?????? ?????? dueDate null ????????? ??????

    try {
      String accessToken = accessTokenGet.split(" ")[1];
      if (!oauthService.validatieTokenInput(accessToken)) {
        return ResponseEntity.status(401).body("1");
      }
      if (!oauthService.isUserExist(accessToken)) {
        return ResponseEntity.status(401).body(" 2");
      }    // ?????? ???????????? ??????

      String userIdx = oauthService.getUserIdx(accessToken);

      if (dueDateGet == "") {
        notificationService.deleteDueDate((long) bookmarkId, Long.valueOf(userIdx));
      }
      if (dueDateGet != null && dueDateGet != "") {
        String dueDateSplittedYear = dueDateGet.split("/")[0];
        String dueDateSplittedMonth = dueDateGet.split("/")[1];
        String dueDateSplittedDate = dueDateGet.split("/")[2];

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
        if (postRepository.getPostByPostIdx((long) bookmarkId).getUserIdx()
            .equals(userIdx)) {

          Post newPost = postRepository.getPostByPostIdx((long) bookmarkId);

          if (requestMemo != null) {
            newPost.setPostMemo(requestMemo);
          }
          if (isRead != null) {
            newPost.setIsRead(isRead);
          }

          // notification ?????? ??????

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
