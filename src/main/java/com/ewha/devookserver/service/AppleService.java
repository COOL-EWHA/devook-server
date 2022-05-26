package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.auth.JwtTokenProvider;
import com.ewha.devookserver.domain.auth.LoginResponse;
import com.ewha.devookserver.domain.user.Member;
import com.ewha.devookserver.domain.user.Role;
import com.ewha.devookserver.repository.MemberRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppleService {

  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;

  /*
  encoded 로부터 사용자의 이름을 가져오는 함수
   */

  public Boolean isAppleUserExists(String idToken) throws Exception {
    return memberRepository.existsMemberByOauthId(userIdFromApple(idToken).get("sub"));
  }


  public LoginResponse appleLogin(String idToken, Boolean existUser, String jsonNameStr)
      throws Exception {
    String refreshToken;
    Member member;

    if (existUser) {
      member = memberRepository.findMemberByOauthId(userIdFromApple(idToken).get("sub"));
      refreshToken = member.getRefreshToken();
    } else {
      refreshToken = jwtTokenProvider.createRefreshToken();
      String name;
      if (jsonNameStr.equals(null)) {
        name = userIdFromApple(idToken).get("email");
      } else {
        name = getUserName(jsonNameStr);
      }
      member = Member.builder()
          .email(userIdFromApple(idToken).get("email"))
          .name(name)
          .oauthId(userIdFromApple(idToken).get("sub"))
          .imageUrl(RandomString.make())
          .refreshToken(refreshToken)
          .role(Role.GUEST)
          .build();

      memberRepository.save(member);
    }

    String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(member.getId()));

    return LoginResponse.builder()
        .id(member.getId())
        .nickname(member.getName())
        .email(member.getEmail())
        .imageUrl(member.getImageUrl())
        .role(member.getRole())
        .tokenType("Bearer")
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .isExistUser(existUser)
        .build();
  }


  public String getUserName(String jsonStr) {

    Object obj = JsonParser.parseString(jsonStr);
    JsonObject jsonObject = (JsonObject) obj;

    String name = String.valueOf(jsonObject.get("name"));
    Object nameInfo = JsonParser.parseString(name);
    JsonObject jsonObject1 = (JsonObject) nameInfo;

    String fullName =
        String.valueOf(jsonObject1.get("firstName")) + jsonObject1.get("lastName");
    String match = "[^\uAC00-\uD7A30-9a-zA-Z]";

    fullName = fullName.replaceAll(match, "");

    return fullName;
  }

  public HashMap<String, String> userIdFromApple(String idToken) throws Exception {
    StringBuffer result = new StringBuffer();
    try {
      URL url = new URL("https://appleid.apple.com/auth/keys");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

      String line = "";

      while ((line = br.readLine()) != null) {
        result.append(line);
      }
    } catch (IOException e) {
      throw new Exception("error");
    }

    JsonParser parser = new JsonParser();
    JsonObject keys = (JsonObject) JsonParser.parseString(result.toString());
    JsonArray keyArray = (JsonArray) keys.get("keys");

    String[] decodeArray = idToken.split("\\.");
    String header = new String(Base64.getDecoder().decode(decodeArray[0]));

    JsonElement kid = ((JsonObject) JsonParser.parseString(header)).get("kid");
    JsonElement alg = ((JsonObject) JsonParser.parseString(header)).get("alg");

    JsonObject avaliableObject = null;
    for (int i = 0; i < keyArray.size(); i++) {
      JsonObject appleObject = (JsonObject) keyArray.get(i);
      JsonElement appleKid = appleObject.get("kid");
      JsonElement appleAlg = appleObject.get("alg");

      if (Objects.equals(appleKid, kid) && Objects.equals(appleAlg, alg)) {
        avaliableObject = appleObject;
        break;
      }
    }

    PublicKey publicKey = this.getPublicKey(avaliableObject);

    Claims userInfo = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(idToken).getBody();
    JsonObject userInfoObject = (JsonObject) JsonParser.parseString(new Gson().toJson(userInfo));

    JsonElement appleAlg = userInfoObject.get("sub");
    String userId = appleAlg.getAsString();

    JsonElement appleAlg2 = userInfoObject.get("email");
    String userEmail = appleAlg2.getAsString();

    HashMap<String, String> map = new HashMap<>();
    map.put("sub", userId);
    map.put("email", userEmail);

    return map;
  }

  public PublicKey getPublicKey(JsonObject object) throws Exception {
    String nStr = object.get("n").toString();
    String eStr = object.get("e").toString();

    byte[] nBytes = Base64.getUrlDecoder().decode(nStr.substring(1, nStr.length() - 1));
    byte[] eBytes = Base64.getUrlDecoder().decode(eStr.substring(1, eStr.length() - 1));

    BigInteger n = new BigInteger(1, nBytes);
    BigInteger e = new BigInteger(1, eBytes);

    try {
      RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
      return publicKey;
    } catch (Exception exception) {
      throw new Exception("error");
    }
  }
}
