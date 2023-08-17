package com.sangbu3jo.elephant.auth.controller;


import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {
  private final AuthService authService;


  @PostMapping("/auth/signup")
  public ResponseEntity<String> signUp(@RequestBody SignupRequestDto signupRequestDto) {
    String result = authService.signup(signupRequestDto);
    return ResponseEntity.ok().body(result);
  }

}
