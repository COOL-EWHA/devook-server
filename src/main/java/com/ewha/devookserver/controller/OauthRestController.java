package com.ewha.devookserver.controller;

import com.ewha.devookserver.config.auth.JwtTokenProvider;
import com.ewha.devookserver.config.auth.LoginResponse;
import com.ewha.devookserver.config.auth.Member;
import com.ewha.devookserver.repository.MemberRepository;
import com.ewha.devookserver.domain.dto.*;
import com.ewha.devookserver.service.OauthService;
import com.ewha.devookserver.service.UserService;
import java.util.Objects;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@RestController
public class OauthRestController {
    private final OauthService oauthService;
    private final UserService userService;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping("/auth/logout")
    public ResponseEntity<?> userLogout(@RequestHeader(value="Authorization")String accessTokenGet,
        HttpServletResponse response) throws Exception{


        System.out.println("@GetMapping /auth/logout");

        String accessToken=accessTokenGet.split(" ")[1];

        if(!oauthService.validatieTokenInput(accessToken)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        System.out.println(oauthService.isUserExist(accessToken));

        try{
            if(!oauthService.isUserExist(accessToken)){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            String userIdx = oauthService.getUserIdx(accessToken);
            oauthService.deleteUserRefreshToken(Long.valueOf(userIdx));


            ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", null)
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .path("/")
                .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (RuntimeException e){
            System.out.println(e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/auth/test-login")
    public ResponseEntity<?> testLogin(HttpServletResponse response, @RequestBody TestLoginDto testLoginDto){


        String userEmail = testLoginDto.getEmail();
        boolean isUserExistemail=userService.isMemberExistByEmail(userEmail);

        if(isUserExistemail){
             if(memberRepository.countMemberByEmail(userEmail)!=1){
                 Member member=userService.ifGoogleUser(userEmail);
                 String userAccessToken=oauthService.getAccessToken(member);

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

                 ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", loginFinalResponseDto.getRefreshToken())
                     .sameSite("None")
                     .secure(true)
                     .httpOnly(true)
                     .path("/")
                     .build();
                 response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                 //    .domain(".example.com")

                 return ResponseEntity.status(200).body(revisedCookieDto);
             }
             else{
                 Member member = userService.returnEmailUSer(userEmail);
                 String userAccessToken=oauthService.getAccessToken(member);

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

                 ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", loginFinalResponseDto.getRefreshToken())
                     .sameSite("None")
                     .secure(true)
                     .httpOnly(true)
                     .path("/")
                     .build();
                 response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                 return ResponseEntity.status(200).body(revisedCookieDto);
             }
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<?> loginRefresh(@RequestHeader(value="Cookie")String refreshTokenGet, HttpServletResponse response){

        try {

            if (Objects.equals(refreshTokenGet, "REFRESH_TOKEN=")) {
                return ResponseEntity.status(400).body("");
            }

            String accessToken = refreshTokenGet.split("=")[1];

            boolean isTokenExists = userService.checkRightRefreshToken(accessToken);

            if (!isTokenExists) {
                return ResponseEntity.status(404).body("");
            } else {
                Member member = userService.returnRefreshTokenMember(accessToken);

                RefreshDto refreshDto = oauthService.refreshUserToken(member);
                RefreshResponseDto refreshResponseDto =
                    RefreshResponseDto.builder().accessToken(refreshDto.getAccessToken())
                        .refreshToken(refreshDto.getRefreshToken()).build();
                ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN",
                        refreshDto.getRefreshToken())
                    .sameSite("None")
                    .secure(true)
                    .httpOnly(true)
                    .path("/")
                    .build();
                //domain "localhost:8080"
                //domain "localhost:3000"
                // domain "https://localhost:3000"
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                return ResponseEntity.status(HttpStatus.OK).body(refreshResponseDto);
            }

        }catch (Exception e){
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(" ");

        }
    }


    @PostMapping("/auth/login/{provider}")
    public ResponseEntity<?> loginUser(@PathVariable String provider, @RequestBody TokenRequestDto tokenRequestDto, HttpServletResponse response) {
        System.out.println("@PostMapping /auth/login/provider");

        String replaceToken= tokenRequestDto.getCode().replace("%2F", "/");

        try{
            LoginResponse loginResponse = oauthService.login(provider, replaceToken);

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
                .build();

            ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", loginFinalResponseDto.getRefreshToken())
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .path("/")
                .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            if(!loginResponse.isExistUser()){
                return ResponseEntity.status(201).body(revisedCookieDto);
            }

            return ResponseEntity.status(200).body(revisedCookieDto);
        }catch (Exception e){
            System.out.println(e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUserInfo(@RequestHeader(value="Authorization")String accessTokenGet, HttpServletResponse response) throws Exception{
        System.out.println("@GetMapping /users");
        String accessToken=accessTokenGet.split(" ")[1];

        if(!oauthService.validatieTokenInput(accessToken)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try{
            if(!oauthService.isUserExist(accessToken)){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            String userIdx=oauthService.getUserIdx(accessToken);

            System.out.println(userIdx);
            Long convertedIdx=Long.valueOf(userIdx);
            Member member= userService.findMemberInfo(convertedIdx);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Set-Cookie", accessToken);
            UserInfoResponseDto userinfo=UserInfoResponseDto.builder()
                    .email(member.getEmail())
                    .nickname(member.getName())
                    .build();
            return ResponseEntity.status(200).headers(httpHeaders).body(userinfo);
        }catch (RuntimeException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/users")
    public ResponseEntity<?> deleteUser(@RequestHeader(value="Authorization")String accessTokenGet){
        String accessToken=accessTokenGet.split(" ")[1];

        try {
            if (!oauthService.isUserExist(accessToken)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            String userIdx = oauthService.getUserIdx(accessToken);
            Long convertedIdx = Long.valueOf(userIdx);
            Member member = userService.findMemberInfo(convertedIdx);

            userService.deleteMember(member.getId());
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (RuntimeException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
