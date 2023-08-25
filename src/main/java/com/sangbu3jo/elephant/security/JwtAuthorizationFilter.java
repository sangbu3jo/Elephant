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
    public static int AututhCNT = 1;

/*    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    // 해당 필터를 거치지 않아야 할 url을 기재하여 처리할 수 있습니다.
        String[] excludePath = {"/" , "/api/auth/login-page" , "/api/auth/login" , "/api/auth/signup",
            "/api/auth/google/callback" , "/api/auth/kakao/callback" , "/api/auth/naver/callback"};
        String path = request.getRequestURI();
        return Arrays.stream(excludePath).anyMatch(path::startsWith);
    }*/

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, IOException {
        //log.info("JwtAuthorizationFilter dofilterInternal " + AututhCNT++);
        String AccesstokenValue = jwtUtil.getAccessTokenFromRequest(request);
        String refreshTokenValue = jwtUtil.getRefreshTokenFromRequest(request);

        if (StringUtils.hasText(AccesstokenValue)) {
            // JWT 토큰 substring
            AccesstokenValue = jwtUtil.substringToken(AccesstokenValue);
            refreshTokenValue = jwtUtil.substringToken(refreshTokenValue);

            if (!jwtUtil.validateToken(AccesstokenValue)) {
                log.error("Access Token does not valid.");

                if(request.getRequestURI().equals("/api/auth/logout")){
                    // 만료된 access token 을 갖고 로그아웃 요청 시 -> 토큰 모두 삭제
                    redisService.deleteRefreshToken(request,response);
                    return;
                }

                // 엑세스 토큰 재발급
                redisService.generateAccessToken(request, response);
                response.sendRedirect("/");
                return;
            }

            Claims info = jwtUtil.getUserInfoFromToken(AccesstokenValue);

            try {
                setAuthentication(info.getSubject());
            } catch (Exception e) {
                log.error(e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // 인증 처리
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}

