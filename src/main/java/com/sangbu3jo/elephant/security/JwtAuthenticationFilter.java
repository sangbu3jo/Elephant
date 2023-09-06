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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

@Slf4j(topic = "JwtAuthenticationFilter 로그인 및 JWT 생성")
  public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
  private final JwtUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;


  /**
   * JwtAuthenticationFilter 클래스의 생성자 메서드
   * @param jwtUtil jwt 관련 빈 객체
   * @param refreshTokenRepository 리프레시 토큰 repository 빈 객체
   */
  public JwtAuthenticationFilter(JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtUtil = jwtUtil;
    setFilterProcessesUrl("/api/auth/login");
  }


  /**
   * 인증을 위한 메서드
   * @param request from which to extract parameters and perform the authentication
   * @param response the response, which may be needed if the implementation has to do a
   * redirect as part of a multi-stage authentication process (such as OIDC).
   * @return 인증 객체
   * @throws AuthenticationException
   */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    String AccessTokenValue = jwtUtil.getAccessTokenFromRequest(request);
    String refreshTokenValue = jwtUtil.getRefreshTokenFromRequest(request);

    if (StringUtils.hasText(AccessTokenValue)||StringUtils.hasText(refreshTokenValue)) {
      jwtUtil.deleteCookie(request,response);
    }

    try {
      LoginRequestDto requestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);

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


  /**
   * 인증 성공 후 메서드
   * @param request 요청 Servlet
   * @param response 응답 Servlet
   * @param chain 시큐리티 필터 체인
   * @param authResult the object returned from the <tt>attemptAuthentication</tt>
   * method.
   * @throws IOException
   */
  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
    log.info("로그인 성공 및 JWT 생성");
    String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
    UserRoleEnum role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getRole();

    // access token 발급 및 쿠키에 저장
    String accessToken = jwtUtil.createToken(username,role);
    jwtUtil.addJwtToCookieAccessToken(accessToken, response);

    //  access token 발급 및 쿠키, Redis 에 저장
    RefreshToken refreshToken = jwtUtil.createRefreshToken(username,role);
    refreshTokenRepository.save(refreshToken);
    jwtUtil.addJwtToCookieRefreshToken(refreshToken.getRefreshToken(),response);

    log.info("로그인 성공");
  }


  /**
   * 인증 실패 후 메서드
   * @param request 요청 Servlet
   * @param response 응답 Servlet
   * @param failed AuthenticationException 인증 예외 객체
   */
  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
    log.info("로그인 실패");
    response.setStatus(401);
  }

}