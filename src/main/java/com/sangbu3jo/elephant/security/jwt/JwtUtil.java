package com.sangbu3jo.elephant.security.jwt;

import com.sangbu3jo.elephant.auth.redis.RefreshToken;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Component
public class JwtUtil {
  // Header KEY 값
  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String REFRESH_HEADER = "RefreshToken";
  // 사용자 권한 값의 KEY
  public static final String AUTHORIZATION_KEY = "auth";

  // Token 식별자
  public static final String BEARER_PREFIX = "Bearer ";

  // 엑세스 토큰 만료시간
  private final long TOKEN_TIME = 60 * 10 * 1000L; // 10분

  @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
  private String secretKey;
  private Key key;
  private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

  // 로그 설정
  public static final Logger logger = LoggerFactory.getLogger("JWT 관련 로그");

  @PostConstruct
  public void init() {
    byte[] bytes = Base64.getDecoder().decode(secretKey);
    key = Keys.hmacShaKeyFor(bytes);
  }

  // 엑세스 토큰 생성
  public String createToken(String username, UserRoleEnum role) {
    Date date = new Date();

    return BEARER_PREFIX +
        Jwts.builder()
            .setSubject(username) // 사용자 식별자값(ID)
            .claim(AUTHORIZATION_KEY, role) // 사용자 권한
            .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간
            .setIssuedAt(date) // 발급일
            .signWith(key, signatureAlgorithm) // 암호화 알고리즘
            .compact();
  }

  // refresh token 생성
  public RefreshToken createRefreshToken(String username, UserRoleEnum role){
    return new RefreshToken(username, UUID.randomUUID().toString());
  }

  // JWT Cookie 에 access token 저장
  public void addJwtToCookieAccessToken(String token, HttpServletResponse res) {
    token = URLEncoder.encode(token, StandardCharsets.UTF_8).replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행

    Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token); // Name-Value
    cookie.setPath("/");
    res.addCookie(cookie);
  }

  // JWT Cookie 에 refresh token 저장
  public void addJwtToCookieRefreshToken(String refreshToken, HttpServletResponse res) {
    refreshToken = URLEncoder.encode(refreshToken, StandardCharsets.UTF_8).replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행

    Cookie cookie = new Cookie(REFRESH_HEADER, refreshToken); // refreshToken
    cookie.setPath("/");
    res.addCookie(cookie);
  }

  // JWT 토큰 substring
  public String substringToken(String tokenValue) {
    if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
      return tokenValue.substring(7);
    }
    logger.error("Not Found Token");
    throw new NullPointerException("Not Found Token");
  }

  // 토큰 검증
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (SecurityException | MalformedJwtException | SignatureException e) {
      logger.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
    } catch (ExpiredJwtException e) {
      logger.error("Expired JWT token, 만료된 JWT token 입니다.");
    } catch (UnsupportedJwtException e) {
      logger.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
    }
    return false;
  }

  // 토큰에서 사용자 정보 가져오기
  public Claims getUserInfoFromToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }


  // HttpServletRequest 에서 Cookie Value : JWT access Token 가져오기
  public String getAccessTokenFromRequest(HttpServletRequest req) {
    Cookie[] cookies = req.getCookies();
    if(cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(AUTHORIZATION_HEADER)) {
          return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8); // Encode 되어 넘어간 Value 다시 Decode
        }
      }
    }
    return null;
  }


  // HttpServletRequest 에서 Cookie Value : JWT refresh Token 가져오기
  public String getRefreshTokenFromRequest(HttpServletRequest req) {
    Cookie[] cookies = req.getCookies();
    if(cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(REFRESH_HEADER)) {
          return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8); // Encode 되어 넘어간 Value 다시 Decode
        }
      }
    }
    return null;
  }


  // 엑세스, 리프레시 쿠키 날짜를 0으로 만들어 만료시킴
  public void deleteCookie(HttpServletRequest request, HttpServletResponse response) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return;
    }
    for (Cookie cookie : cookies) {
      if(cookie.getName().equals(AUTHORIZATION_HEADER)
          | cookie.getName().equals(REFRESH_HEADER)){
        cookie.setMaxAge(0);
        response.addCookie(cookie);
      }
    }
  }



}
