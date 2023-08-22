package com.sangbu3jo.elephant.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangbu3jo.elephant.auth.dto.LoginRequestDto;
import com.sangbu3jo.elephant.auth.redis.RefreshToken;
import com.sangbu3jo.elephant.auth.redis.RefreshTokenRepository;
import com.sangbu3jo.elephant.security.jwt.JwtUtil;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j(topic = "JwtAuthenticationFilter 로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
  private final JwtUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtUtil = jwtUtil;
    setFilterProcessesUrl("/api/auth/login");
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
//1. **로그인 요청 읽기:** `attemptAuthentication` 메서드는 들어오는 HTTP 요청의 입력 스트림에서 로그인 요청 데이터를 읽습니다.
// 입력 데이터는 'LoginRequestDto' 클래스의 형식이어야 하며 사용자 이름과 비밀번호에 대한 필드가 포함될 수 있습니다.
// 2. **인증 토큰 만들기:** 다음 단계는 `UsernamePassword 인스턴스를 만드는 것입니다.
    try {
      LoginRequestDto requestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);
      log.info("attemptAuthentication: " + requestDto.getUsername() + ", " + requestDto.getPassword());
      return getAuthenticationManager().authenticate(
          new UsernamePasswordAuthenticationToken(
              requestDto.getUsername(),
              requestDto.getPassword(),
              null
          )
      );
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
    log.info("로그인 성공 및 JWT 생성");
    String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
    UserRoleEnum role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getRole();

    // access token 발급 및 쿠키에 저장
    String accessToken = jwtUtil.createToken(username,role);
    jwtUtil.addJwtToCookie(accessToken, response);

    // access token 재발급을 위한 refresh token 을 uuid 값으로 생성함.
    RefreshToken refreshToken = new RefreshToken(username, UUID.randomUUID().toString());
    refreshTokenRepository.save(refreshToken); // Redis 에 저장

    jwtUtil.addJwtToCookieRefreshToken(refreshToken.getRefreshToken(),response);
    log.info("로그인 성공");
  }

  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
    log.info("로그인 실패");
    response.setStatus(401);
  }

}