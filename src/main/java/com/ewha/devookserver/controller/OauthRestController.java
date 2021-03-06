package com.ewha.devookserver.controller;

import com.ewha.devookserver.domain.auth.JwtTokenProvider;
import com.ewha.devookserver.domain.auth.LoginResponse;
import com.ewha.devookserver.domain.user.Member;
import com.ewha.devookserver.dto.auth.LoginFinalResponseDto;
import com.ewha.devookserver.dto.auth.RefreshDto;
import com.ewha.devookserver.dto.auth.RefreshResponseDto;
import com.ewha.devookserver.dto.auth.RevisedCookieDto;
import com.ewha.devookserver.dto.auth.TestLoginDto;
import com.ewha.devookserver.dto.auth.TokenRequestDto;
import com.ewha.devookserver.dto.auth.UserInfoResponseDto;
import com.ewha.devookserver.repository.MemberRepository;
import com.ewha.devookserver.service.AppleService;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.UserService;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OauthRestController {

  private final OauthService oauthService;
  private final UserService userService;
  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final AppleService appleService;

  // @ 0217 09:00 ???????????? test server ver.

  @PostMapping("/auth/logout")
  public ResponseEntity<?> userLogout(@RequestHeader(value = "Authorization") String accessTokenGet,
      HttpServletResponse response) throws Exception {

    String accessToken = accessTokenGet.split(" ")[1];

    if (!oauthService.validatieTokenInput(accessToken)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    System.out.println(oauthService.isUserExist(accessToken));

    try {
      if (!oauthService.isUserExist(accessToken)) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      String userIdx = oauthService.getUserIdx(accessToken);
      // oauthService.deleteUserRefreshToken(Long.valueOf(userIdx));

      ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", null)
          .maxAge(0)
          .httpOnly(true)
          .path("/")
          .build();
      response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (RuntimeException e) {
      System.out.println(e);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("/auth/test-login")
  public ResponseEntity<?> testLogin(HttpServletResponse response,
      @RequestBody TestLoginDto testLoginDto) {

    String refreshToken = testLoginDto.getRefreshToken();
    boolean isUserExistRefreshToken = userService.isMemberExistByUserRefreshToken(refreshToken);

    if (isUserExistRefreshToken) {
      Member member = memberRepository.findMemberByRefreshToken(refreshToken);
      String userAccessToken = oauthService.getAccessToken(member);

      LoginFinalResponseDto loginFinalResponseDto = LoginFinalResponseDto.builder()
          .email(member.getEmail())
          .nickname(member.getName())
          .accessToken(userAccessToken)
          .refreshToken(member.getRefreshToken())
          .build();

      RevisedCookieDto revisedCookieDto = RevisedCookieDto.builder()
          .email(loginFinalResponseDto.getEmail())
          .nickname(loginFinalResponseDto.getNickname())
          .accessToken(loginFinalResponseDto.getAccessToken())
          .build();

      ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN",
              loginFinalResponseDto.getRefreshToken())
          .httpOnly(true)
          .path("/")
          .build();
      response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

      //    .domain(".example.com")

      return ResponseEntity.status(200).body(revisedCookieDto);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<?> loginRefresh(
      @RequestHeader(value = "Cookie", required = false) String refreshTokenGet,
      @RequestBody(required = false) TestLoginDto testLoginDto,
      HttpServletRequest request,
      HttpServletResponse response) {

    String accessToken = "no";
    Boolean cookieValid = true;

    String accessTokenBody;

    try {
      Cookie[] list = request.getCookies();
      for (Cookie cookie : list) {
        if (cookie.getName().equals("REFRESH_TOKEN")) {
          accessToken = cookie.getValue();
        }
      }

    } catch (Exception e1) {
      cookieValid = false;
    }

    if (cookieValid) {

      if (accessToken.equals("no")) {
        return ResponseEntity.status(404).body("??????");
      }
      boolean isTokenExists = userService.checkRightRefreshToken(accessToken);

      if (!isTokenExists) {
        return ResponseEntity.status(404).body("222");
      } else {
        Member member = userService.returnRefreshTokenMember(accessToken);

        RefreshDto refreshDto = oauthService.refreshUserToken(member);
        RefreshResponseDto refreshResponseDto =
            RefreshResponseDto.builder().accessToken(refreshDto.getAccessToken())
                .refreshToken(refreshDto.getRefreshToken()).build();

        return ResponseEntity.status(HttpStatus.OK).body(refreshResponseDto);

      }
    } else {
      try {
        accessTokenBody = testLoginDto.getRefreshToken();
      } catch (Exception e) {
        accessTokenBody = null;
      }

      if (accessTokenBody == null) {
        return ResponseEntity.status(404).body("body ??? null ");
      }
      accessTokenBody = testLoginDto.getRefreshToken();
      boolean isTokenExistsBody = userService.checkRightRefreshToken(
          testLoginDto.getRefreshToken());

      if (!isTokenExistsBody) {
        return ResponseEntity.status(404).body("????????? ???????????? ?????? ??????");
      } else {
        Member member = userService.returnRefreshTokenMember(accessTokenBody);

        RefreshDto refreshDto = oauthService.refreshUserToken(member);
        RefreshResponseDto refreshResponseDto =
            RefreshResponseDto.builder().accessToken(refreshDto.getAccessToken())
                .refreshToken(refreshDto.getRefreshToken()).build();

        return ResponseEntity.status(HttpStatus.OK).body(refreshResponseDto);
      }
    }
  }

  @RequestMapping(value = "/auth/login/apple", method = {RequestMethod.GET,
      RequestMethod.POST}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> appleLogin(@RequestBody MultiValueMap<String, String> formParmameters,
      HttpServletResponse response) throws Exception {
    String id_token = formParmameters.getFirst("id_token");

    Boolean existUser = appleService.isAppleUserExists(id_token);
    LoginResponse loginResponse;

    if (existUser) {
      loginResponse = appleService.appleLogin(id_token, true, null);

    } else {
      try {
        loginResponse = appleService.appleLogin(id_token, false, formParmameters.getFirst("user"));
      } catch (Exception e) {
        loginResponse = appleService.appleLogin(id_token, false, null);
      }
    }

    LoginFinalResponseDto loginFinalResponseDto = LoginFinalResponseDto.builder()
        .email(loginResponse.getEmail())
        .nickname(loginResponse.getNickname())
        .accessToken(loginResponse.getAccessToken())
        .refreshToken(loginResponse.getRefreshToken())
        .build();

    RevisedCookieDto revisedCookieDto = RevisedCookieDto.builder()
        .email(loginResponse.getEmail())
        .nickname(loginResponse.getNickname())
        .accessToken(loginResponse.getAccessToken())
        .refreshToken(loginResponse.getRefreshToken())
        .build();

    ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN",
            loginFinalResponseDto.getRefreshToken())
        .httpOnly(true)
        .path("/")
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    response.sendRedirect("https://www.devook.com");

    if (!loginResponse.isExistUser()) {
      return ResponseEntity.status(201).body(revisedCookieDto);
    }
    return ResponseEntity.status(200).body(revisedCookieDto);
  }

  @RequestMapping(value = "/auth/login/google", method = {RequestMethod.GET, RequestMethod.POST})
  public ResponseEntity<?> loginUser(
      @RequestBody TokenRequestDto tokenRequestDto, HttpServletResponse response) {
    System.out.println("@PostMapping /auth/login/provider");

    String replaceToken = tokenRequestDto.getCode().replace("%2F", "/");
    System.out.println(tokenRequestDto.getCode());

    try {
      LoginResponse loginResponse = oauthService.login("google", replaceToken);

      LoginFinalResponseDto loginFinalResponseDto = LoginFinalResponseDto.builder()
          .email(loginResponse.getEmail())
          .nickname(loginResponse.getNickname())
          .accessToken(loginResponse.getAccessToken())
          .refreshToken(loginResponse.getRefreshToken())
          .build();

      RevisedCookieDto revisedCookieDto = RevisedCookieDto.builder()
          .email(loginResponse.getEmail())
          .nickname(loginResponse.getNickname())
          .accessToken(loginResponse.getAccessToken())
          .refreshToken(loginResponse.getRefreshToken())
          .build();

      ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN",
              loginFinalResponseDto.getRefreshToken())
          .httpOnly(true)
          .path("/")
          .build();
      response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

      if (!loginResponse.isExistUser()) {
        return ResponseEntity.status(201).body(revisedCookieDto);
      }

      return ResponseEntity.status(200).body(revisedCookieDto);
    } catch (Exception e) {
      System.out.println(e);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }


  @GetMapping("/users")
  public ResponseEntity<?> getUserInfo(
      @RequestHeader(value = "Authorization") String accessTokenGet, HttpServletResponse response)
      throws Exception {
    System.out.println("@GetMapping /users");
    String accessToken = accessTokenGet.split(" ")[1];

    if (!oauthService.validatieTokenInput(accessToken)) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    try {
      if (!oauthService.isUserExist(accessToken)) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      String userIdx = oauthService.getUserIdx(accessToken);

      System.out.println(userIdx);
      Long convertedIdx = Long.valueOf(userIdx);
      Member member = userService.findMemberInfo(convertedIdx);

      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.add("Set-Cookie", accessToken);
      UserInfoResponseDto userinfo = UserInfoResponseDto.builder()
          .email(member.getEmail())
          .nickname(member.getName())
          .build();
      return ResponseEntity.status(200).headers(httpHeaders).body(userinfo);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/users")
  public ResponseEntity<?> deleteUser(
      @RequestHeader(value = "Authorization") String accessTokenGet) {
    String accessToken = accessTokenGet.split(" ")[1];

    try {
      if (!oauthService.isUserExist(accessToken)) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      String userIdx = oauthService.getUserIdx(accessToken);
      Long convertedIdx = Long.valueOf(userIdx);
      Member member = userService.findMemberInfo(convertedIdx);

      userService.deleteMember(member.getId());
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
