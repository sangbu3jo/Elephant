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
  private final long ACCESS_TOKEN_TIME = 60 * 60 * 1000L; // 1시간

  // 리프레시 토큰 만료시간
  private final long REFRESH_TOKEN_TIME = 60 * 60 * 24 * 1000L; // 24시간


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


  /**
   * 엑세스 토큰 생성 메서드
   * @param username 사용자 id값
   * @param role 사용자 권한
   * @return 생성된 jwt 토큰값
   */
  public String createToken(String username, UserRoleEnum role) {
    Date date = new Date();

    return BEARER_PREFIX +
        Jwts.builder()
            .setSubject(username) // 사용자 식별자값(ID)
            .claim(AUTHORIZATION_KEY, role) // 사용자 권한
            .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_TIME)) // 만료 시간
            .setIssuedAt(date) // 발급일
            .signWith(key, signatureAlgorithm) // 암호화 알고리즘
            .compact();
  }


  /**
   * refresh token 생성 메서드
   * @param username 사용자 id값
   * @param role 사용자 권한
   * @return RefreshToken 토큰값
   */
  public RefreshToken createRefreshToken(String username, UserRoleEnum role){
    Date date = new Date();

    return new RefreshToken(username,BEARER_PREFIX +
        Jwts.builder()
            .setSubject(username) // 사용자 식별자값(ID)
            .claim(AUTHORIZATION_KEY, role) // 사용자 권한
            .setExpiration(new Date(date.getTime() + REFRESH_TOKEN_TIME)) // 만료 시간
            .setIssuedAt(date) // 발급일
            .signWith(key, signatureAlgorithm) // 암호화 알고리즘
            .compact());
  }


  /**
   * JWT Cookie 에 access token 저장 메서드
   * @param token access token
   * @param res 요청 Servlet
   */
  public void addJwtToCookieAccessToken(String token, HttpServletResponse res) {
    token = URLEncoder.encode(token, StandardCharsets.UTF_8).replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행

    Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token); // 쿠키 생성
    cookie.setPath("/");
    cookie.setMaxAge(60*60); // 1시간
    cookie.setSecure(true);
    res.addCookie(cookie);
  }


  /**
   * JWT Cookie 에 refresh token 저장 메서드
   * @param refreshToken refresh token
   * @param res 요청 Servlet
   */
  public void addJwtToCookieRefreshToken(String refreshToken, HttpServletResponse res) {
    refreshToken = URLEncoder.encode(refreshToken, StandardCharsets.UTF_8).replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행

    Cookie cookie = new Cookie(REFRESH_HEADER, refreshToken); // Name-Value
    cookie.setPath("/");
    cookie.setMaxAge(60*60*24); // 24시간
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    res.addCookie(cookie);
  }


  /**
   * JWT 토큰 substring 메서드
   * @param tokenValue "Bearer " 을 substring 할 token 값
   * @return "Bearer " 을 substring 한 token 값
   */
  public String substringToken(String tokenValue) {
    if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
      return tokenValue.substring(7);
    }
    logger.error("Not Found Token");
    throw new IllegalArgumentException("Not Found Token");
  }


  /**
   * 토큰 검증 메서드
   * @param token 검증할 토큰값
   * @return 검증 여부 boolean 값으로 반환
   */
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


  /**
   * 토큰에서 사용자 정보 가져오기
   * @param token "Bearer " 을 substring 한 token 값
   * @return 사용자 정보가 저장된 Claims 객체
   */
  public Claims getUserInfoFromToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }

  /**
   * 사용자 권한 가져오기
   * @param info 사용자 정보가 저장된 Claims 객체
   * @return 사용자 권한 UserRoleEnum 타입으로 반환
   */
  public UserRoleEnum getUserRole(Claims info) {
    String roleValue = info.get(AUTHORIZATION_KEY).toString();
    return roleValue.equals("USER") ? UserRoleEnum.USER : UserRoleEnum.ADMIN;
  }


  /**
   * HttpServletRequest 에서 Cookie Value : JWT access Token 가져오기
   * @param req 요청 Servlet
   * @return JWT access Token 값
   */
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

  /**
   * HttpServletRequest 에서 Cookie Value : JWT refresh Token 가져오기
   * @param req 요청 Servlet
   * @return JWT refresh Token 값
   */
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

  /**
   * 엑세스, 리프레시 쿠키 날짜를 0으로 만들어 만료시키는 메서드
   * @param request 요청 Servlet
   * @param response 응답 Servlet
   */
  public void deleteCookie(HttpServletRequest request, HttpServletResponse response) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return;
    }
    for (Cookie cookie : cookies) {
      if(cookie.getName().equals(AUTHORIZATION_HEADER)
          | cookie.getName().equals(REFRESH_HEADER)){
        cookie.setValue(""); // Clear the value of the cookie
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
      }
    }
  }



}
