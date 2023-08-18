package com.sangbu3jo.elephant.auth.service;

import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;

public interface AuthService {

  /**
   * 회원가입 메서드
   * @param signupRequestDto 회원 가입 요청 정보 데이터 ( username, nickname, introduction)
   * @return 성공 여부 String 타입으로 반환
   */
  String signup(SignupRequestDto signupRequestDto);
}
