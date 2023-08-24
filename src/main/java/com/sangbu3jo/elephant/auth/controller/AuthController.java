package com.sangbu3jo.elephant.auth.controller;


import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.auth.service.AuthServiceImpl;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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


  // 회원가입 메서드 구현
  @PostMapping("/auth/signup")
  public ResponseEntity<String> signUp(
      @RequestBody @Valid SignupRequestDto requestDto) {
    String result = authService.signup(requestDto);
    return ResponseEntity.ok().body(result);
  }


  // 로그아웃 메서드 구현
  @DeleteMapping("/auth/logout")
  public ResponseEntity<String> logout(
      @AuthenticationPrincipal UserDetailsImpl userDetails){
    String result = authService.logout(userDetails.getUser());
    return ResponseEntity.ok(result);
  }


  // 만료 전 access token 재발급
  @GetMapping("/auth/refresh/access-token")
  public ResponseEntity<String> generateRefreshToken(
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    boolean result = authService.generateAccessToken(request, response);
    if(result) {
      return ResponseEntity.ok("Access Token 생성 성공");
    }else {
      return ResponseEntity.badRequest().body("Access Token 생성 실패");
    }
  }


}
