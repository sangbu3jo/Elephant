package com.sangbu3jo.elephant.auth.redis;


import com.sangbu3jo.elephant.security.jwt.JwtUtil;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum.Authority;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j(topic = "Redis Service")
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtUtil jwtUtil;

  @Override
  public void generateRefreshToken(String inputToken, HttpServletRequest request, HttpServletResponse response) {
    // 유저 정보 추출
    Claims claims = jwtUtil.getUserInfoFromToken(inputToken);
    String username = claims.getSubject();
    UserRoleEnum role = jwtUtil.getUserRole(claims);
    log.info("username: " + username + ", role: " + role);

    // Redis 의 리프레시 토큰과 일치 여부 판단
    RefreshToken refreshToken = refreshTokenRepository.findByUsername(username).get();
    String refreshTokenValue = jwtUtil.substringToken(refreshToken.getRefreshToken());
    if (inputToken.equals(refreshTokenValue)) {
      log.info("refresh token 일치");
      // 엑세스 토큰 생성
      createAccessToken(response, username, role);
      return;
    }
    // 다를 경우 쿠키의 토큰값 삭제
    jwtUtil.deleteCookie(request,response);
}

  private void createAccessToken(HttpServletResponse response, String username, UserRoleEnum role) {
    // access token 발급 및 쿠키에 저장
    String accessToken = jwtUtil.createToken(username, role);
    jwtUtil.addJwtToCookieAccessToken(accessToken, response);
  }


}
