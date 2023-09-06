package com.sangbu3jo.elephant.email.service;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.MimeMessagePreparator;

public interface EmailCheckService {

  /**
   * 이메일 인증을 위한 인증 번호를 보내는 메서드
   * @param userEmail 인증 절차를 밟을 이메일 주소
   * @return 메일 수신 여부에 따른 상태값과 결과값 반환
   * @throws Exception
   */
  ResponseEntity<String> sendEmail(String userEmail) throws Exception;


  /**
   * DB에 인증번호 저장하는 메서드
   * @param userEmail 사용자 이메일 주소
   * @param password 이메일에 따른 인증 번호
   */
  void saveEmailPassword(String userEmail, String password);



  /**
   * 인증번호 여부 확인 처리 메서드
   * @param userEmail 인증번호를 구분할 이메일 주소
   * @param inputPassword 입력받은 인증번호
   * @return 처리에 따른 결과값과 상태값
   */
  ResponseEntity<String> checkPassword(String userEmail, String inputPassword);


  /**
   * Redis 에서 이메일 인증번호 조회
   * @param userEmail 인증번호를 구분할 이메일 주소
   * @return 이메일에 따른 인증번호
   */
  String getEmailPassword(String userEmail);


  /**
   * 중복된 이메일로 가입한 회원이 있는지 여부 체크 메서드
   * @param userEmail 수신할 이메일 주소
   */
  void isExistUsername(String userEmail);


  /**
   * 이메일 제목 밑 내용을 설정하는 메서드
   * @param userEmail 인증할 이메일 주소
   * @param password 인증번호
   * @return MimeMessagePreparator 타입의 객체
   */
  MimeMessagePreparator setEmailText(String userEmail, String password);


  /**
   * 이메일 내용 설정하는 메서드
   * @param sendTo 인증 절차를 밟을 이메일 주소
   * @param password 인증 번호
   * @return 설정한 메일 내용 반환
   */
  String setMailMessage(String sendTo, String password);
}
