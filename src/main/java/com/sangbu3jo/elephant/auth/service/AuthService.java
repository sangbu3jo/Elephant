package com.sangbu3jo.elephant.auth.service;

import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.users.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AuthService {

  /**
   * 회원가입 메서드
   * @param signupRequestDto 회원 가입 요청 정보 데이터 ( username, nickname, introduction)
   * @return 성공 여부 String 타입으로 반환
   */
  String signup(SignupRequestDto signupRequestDto);

  /**
   * Access token 갱신 메서드
   * @param request 요청 Servlet
   * @param response 응답 Servlet
   * @return 성공 여부 msg
   */
  Boolean generateAccessToken(HttpServletRequest request, HttpServletResponse response);


  /**
   * 로그아웃 메서드
   * @param request 요청 Servlet
   * @param response 응답 Servlet
   * @param user 유저 사용자 Entity
   * @return 성공 여부 msg
   */
  String logout(HttpServletRequest request, HttpServletResponse response,User user);
}
