package com.sangbu3jo.elephant.security;

import com.sangbu3jo.elephant.auth.redis.RedisServiceImpl;
import com.sangbu3jo.elephant.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
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
   * @param request 요청 Servlet
   * @param response 응답 Servlet
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

    if (StringUtils.hasText(AccessTokenValue)) { // 엑세스 토큰 있을 때

      // 로그인 페이지 요청 시 -> 토큰 모두 삭제
      if (request.getRequestURI().equals("/api/auth/login-page")) {
        jwtUtil.deleteCookie(request, response);
      } else {
        // 엑세스 토큰 substring
        AccessTokenValue = jwtUtil.substringToken(AccessTokenValue);

        // 엑세스 토큰 만료 시 if() 내에 코드를 수행합니다.
        if (!jwtUtil.validateToken(AccessTokenValue)) {
          log.error("Access Token does not valid.");

          if (request.getRequestURI().equals("/api/auth/logout")) { // 만료된 토큰으로 로그아웃 요청 시
            jwtUtil.deleteCookie(request, response); // 토큰 모두 삭제
            return;
          }else { // 다른 url 로 접근 시

            if (StringUtils.hasText(refreshTokenValue)) { // 리프레스 토큰 있을 때
              refreshTokenValue = jwtUtil.substringToken(refreshTokenValue);
              // 엑세스 토큰 재발급
              redisService.generateAccessToken(request, response);
              response.sendRedirect(request.getRequestURI());
              return;
            } else { //리프레시 토큰 없을 때
              jwtUtil.deleteCookie(request, response);
              return;
            } // end of inner if~else()
          } // end of outer if~else()
        } // end of inner if()

        // 엑세스 토큰 유효성 검사 통과 시 아래 코드를 수행합니다.
        Claims info = jwtUtil.getUserInfoFromToken(AccessTokenValue);

        try {
          setAuthentication(info.getSubject());
        } catch (Exception e) {
          log.error(e.getMessage());
          return;
        }

      } // end of if~else()

    } // end of if()

    filterChain.doFilter(request, response);
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
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

}

