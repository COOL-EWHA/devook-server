package com.ewha.devookserver.domain.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    /*
    @Value("${jwt.access-token.expire-length:10000}")
    private long accessTokenValidityInMilliseconds;
    @Value("${jwt.refresh-token.expire-length:10000}")
    private long refreshTokenValidityInMilliseconds;
    @Value("${jwt.token.secret-key:secret-key}")
    private String secretKey;
     */

  // private long accessTokenValidityInMilliseconds=1000 * 60 * 60 * 2;


  // 테스트용으로 1분으로 설정해보자
  private final long accessTokenValidityInMilliseconds = 1000 * 60 * 60 * 2;

  private final long refreshTokenValidityInMilliseconds = 1000 * 60 * 60 * 24 * 14;

  @Value("${jwt.token.secret-key:secret-key}")
  private String secretKey;


  public String createAccessToken(String payload) {
    return createToken(payload, accessTokenValidityInMilliseconds);
  }

  public String createRefreshToken() {
    byte[] array = new byte[7];
    new Random().nextBytes(array);
    String generatedString = new String(array, StandardCharsets.UTF_8);
    return createToken(generatedString, refreshTokenValidityInMilliseconds);
  }

  public String createToken(String payload, long expireLength) {
    Claims claims = Jwts.claims().setSubject(payload);
    Date now = new Date();
    Date validity = new Date(now.getTime() + expireLength);

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(validity)
        .signWith(SignatureAlgorithm.HS256, secretKey)
        .compact();
  }

  public String getPayload(String token) {
    try {
      return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    } catch (ExpiredJwtException e) {
      return e.getClaims().getSubject();
    } catch (JwtException e) {
      throw new RuntimeException("유효하지 않은 토큰");
    }
  }

  public boolean validateToken(String token) {
    try {
      Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

      return !claims.getBody().getExpiration().before(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public boolean validateCheckWeekToken(String token) {
    try {
      Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

      Date currentDate = new Date();
      Date WeekFromCurrentDate = new Date(currentDate.getTime() + Duration.ofDays(7).toMillis());
      return !claims.getBody().getExpiration().before(WeekFromCurrentDate);
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
