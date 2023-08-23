package com.sangbu3jo.elephant.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sangbu3jo.elephant.auth.dto.APITokenDto;
import com.sangbu3jo.elephant.auth.dto.APIUserInfoDto;
import com.sangbu3jo.elephant.users.entity.User;
import java.io.UnsupportedEncodingException;

public interface SocialService {

  /**
   * 소셜 로그인 메서드
   *
   * @param code 소셜 api 를 통해 전달 받은 인가 코드
   * @return JWT 토큰 반환
   * @throws JsonProcessingException json 활용 시에 예외 throw
   */
  APITokenDto socialLogin(String code) throws JsonProcessingException, UnsupportedEncodingException;

  /**
   * 애플리케이션은 인증 코드로 카카오 서버에 토큰을 요청하고, 토큰을 전달 받습니다.
   * 인가 코드를 통해 "액세스 토큰" 요청 메서드
   * @param code 소셜 api 를 통해 전달 받은 인가 코드
   * @return 액세스 토큰
   * @throws JsonProcessingException json 활용 시에 예외 throw
   */
  String getToken(String code) throws JsonProcessingException, UnsupportedEncodingException;

  /**
   * 인가 토큰을 통해 사용자 정보 가져오기
   * @param accessToken 액세스 토큰
   * @return 엑세스 토큰을 통해 추출한 회원 정보
   * @throws JsonProcessingException json 활용 시에 예외 throw
   */
  APIUserInfoDto getUserInfo(String accessToken) throws JsonProcessingException;


  /**
   * 카카오 ID 정보로 회원가입
   * @param apiUserInfoDto 엑세스 토큰을 통해 추출한 회원 정보
   * @return 회원가입 후에 User entity 객체
   */
  User registerUserIfNeeded(APIUserInfoDto apiUserInfoDto);


}
