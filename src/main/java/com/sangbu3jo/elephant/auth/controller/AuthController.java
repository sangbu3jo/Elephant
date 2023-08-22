package com.sangbu3jo.elephant.auth.controller;


import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.auth.service.AuthServiceImpl;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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


  // 로그아웃 메서드 구현 요망


  // 만료된 access token 으로, 만료 전 refresh token
  @GetMapping("/auth/refresh/access-token")
  public ResponseEntity<String> generateRefreshToken(
      HttpServletRequest request, HttpServletResponse response,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    String result = authService.generateRefreshToken(request, response, userDetails.getUser());
    return ResponseEntity.ok(result);
  }


}
