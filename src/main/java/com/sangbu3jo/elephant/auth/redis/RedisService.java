package com.sangbu3jo.elephant.auth.redis;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface RedisService {

  /**
   * 엑세스 토큰 재발급 메서드
   * @param refreshTokenValue 클라이언트 측 Refresh token
   * @param request 요청 Servlet
   * @param response 응답 Servlet
   */

  void generateRefreshToken(String refreshTokenValue, HttpServletRequest request, HttpServletResponse response);
}
