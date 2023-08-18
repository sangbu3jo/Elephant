package com.sangbu3jo.elephant.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/view")
public class AuthViewController {

  // 사용자 로그인 페이지
  @GetMapping("/login")
  public String loginPage() {
    return "login-page";
  }


}