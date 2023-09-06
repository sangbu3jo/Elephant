package com.sangbu3jo.elephant.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/api")
public class AuthViewController {

  /**
   * 사용자 로그인 페이지 url 메서드
   * @return 로그인 페이지 .html 파일명
   */
  @GetMapping("/auth/login-page")
  public String loginPage() {
    return "login-page";
  }


}