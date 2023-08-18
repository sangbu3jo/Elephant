package com.sangbu3jo.elephant.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sangbu3jo.elephant.auth.service.GoogleServiceImpl;
import com.sangbu3jo.elephant.auth.service.KakaoServiceImpl;
import com.sangbu3jo.elephant.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class APILoginController {

  private final KakaoServiceImpl kakaoService;
  private final GoogleServiceImpl googleService;
  private final JwtUtil jwtUtil;

  @GetMapping("/auth/kakao/callback")
  public String kakaoLogin(@RequestParam String code, HttpServletResponse response)
      throws JsonProcessingException {
    String token = kakaoService.socialLogin(code);
    jwtUtil.addJwtToCookie(token,response);
    return "redirect:/";
  }

  @GetMapping("/auth/google/callback")
  public String googleLogin(@RequestParam String code, HttpServletResponse response)
      throws JsonProcessingException {
    String token = googleService.socialLogin(code);
    jwtUtil.addJwtToCookie(token, response);
    return "redirect:/";
  }


}
