package com.ewha.devookserver.controller;

import com.ewha.devookserver.service.UserBookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TestController {
  private final UserBookmarkService userBookmarkService;

  @GetMapping("/test/test")
  public void example(){

    userBookmarkService.bookmarkExcludeUserPosts(64L);
  }

}
