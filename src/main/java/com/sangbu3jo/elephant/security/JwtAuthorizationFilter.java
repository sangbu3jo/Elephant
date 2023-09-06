package com.sangbu3jo.elephant.security;

import com.sangbu3jo.elephant.auth.redis.RedisServiceImpl;
import com.sangbu3jo.elephant.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final RedisServiceImpl redisService;
  private final UserDetailsServiceImpl userDetailsService;


  /**
   * 토큰을 검증하여 사용자의 인증을 처리하는 필터
   * @param request     요청 Servlet
   * @param response    응답 Servlet
   * @param filterChain 보안 필터 체인 객체
   * @throws ServletException
   * @throws IOException
   * @throws IOException
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException, IOException {
    log.info("doFilterInternal");
    String AccessTokenValue = jwtUtil.getAccessTokenFromRequest(request);
    String refreshTokenValue = jwtUtil.getRefreshTokenFromRequest(request);

    // 로그인 페이지 요청 시 -> 토큰 모두 삭제
    if (request.getRequestURI().equals("/api/auth/login-page")) {
      jwtUtil.deleteCookie(request, response);
    } else {
      // 엑세스 토큰 유효할 경우
      if(StringUtils.hasText(AccessTokenValue)){
        AccessTokenValue = jwtUtil.substringToken(AccessTokenValue);
        if(jwtUtil.validateToken(AccessTokenValue)) {
          Claims info = jwtUtil.getUserInfoFromToken(AccessTokenValue);
          try {
            setAuthentication(info.getSubject());
          } catch (Exception e) {
            log.error(e.getMessage());
            return;
          }
        }

      } else if(StringUtils.hasText(refreshTokenValue)) {
        // 엑세스 토큰은 유효하지 않으나, 리프레시 토큰이 있을 경우
        log.error("There is no Access Token. But has RefreshToken");
        createNewAccessToken(request, response, refreshTokenValue);
        return;
      }else {
        // 구현없습니다.
      }

    } // end of if~else()

    filterChain.doFilter(request, response);
  }

  /**
   * 리프레시 토큰을 검증하고 엑세스 토큰을 재발급 하는 메서드
   * @param request 요청 Servlet
   * @param response 응답 Servlet
   * @param refreshTokenValue 리프레시 토큰 값
   * @throws IOException
   */
  private void createNewAccessToken(HttpServletRequest request, HttpServletResponse response,
      String refreshTokenValue) throws IOException {
      refreshTokenValue = jwtUtil.substringToken(refreshTokenValue);
      redisService.generateAccessToken(request, response);
      response.sendRedirect(request.getRequestURI());
  }

  /**
   * 인증 처리 메서드
   * @param username 사용자 id값
   */
  public void setAuthentication(String username) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    Authentication authentication = createAuthentication(username);
    context.setAuthentication(authentication);

    SecurityContextHolder.setContext(context);
  }

  /**
   * 인증 객체 생성 메서드
   * @param username 사용자 id값
   * @return 인증 객체 반환
   */
  private Authentication createAuthentication(String username) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    return new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());
  }

}

