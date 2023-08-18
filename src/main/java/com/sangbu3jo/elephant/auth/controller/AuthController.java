package com.sangbu3jo.elephant.auth.controller;


import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.auth.service.AuthServiceImpl;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

  private final AuthServiceImpl authService;


  @PostMapping("/auth/signup")
  public ResponseEntity<String> signUp(
      @RequestBody @Valid SignupRequestDto requestDto) {
    String result = authService.signup(requestDto);
    return ResponseEntity.ok().body(result);
  }

}
