package com.sangbu3jo.elephant.email.service;

import com.sangbu3jo.elephant.auth.redis.EmailAuth;
import com.sangbu3jo.elephant.auth.redis.EmailAuthRepository;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCheckService {

  private final JavaMailSender javaMailSender;
  private final UserRepository userRepository;
  private final CacheManager redisCacheManager;
  private final EmailAuthRepository emailAuthRepository;


  /**
   * 이메일 인증을 위한 인증 번호를 보내는 메서드
   * @param userEmail 인증 절차를 밟을 이메일 주소
   * @return 메일 수신 여부에 따른 상태값과 결과값 반환
   * @throws Exception
   */
  @Async
  public ResponseEntity<String> sendEmail(String userEmail) throws Exception {
    isExistUsername(userEmail);

    String password = generateAndCachePassword(userEmail);
    emailAuthRepository.save(new EmailAuth(userEmail,password));

    try {
      javaMailSender.send(setEmailText(userEmail,password));
      return ResponseEntity.ok("이메일 전송 성공");
    } catch (MailException e) {
      e.printStackTrace();
      throw new Exception("메일 보내는 도중 오류 발생");
    }
  }

  public ResponseEntity<String> checkPassword(String userEmail, String inputPassword) {

    String password = emailAuthRepository.findByUsername(userEmail).get().getPassword();
    log.info("userEmail: " + userEmail + ", password: " + inputPassword + ", checkPW: " + password);

    if(password.equals(inputPassword)){
      log.info("인증 일치 성공");
      return ResponseEntity.ok("인증 처리 완료");
    }else {
      log.info("인증 불일치 실패");
      return ResponseEntity.badRequest().body("인증 번호 상이");
    }
  }

  
  /**
   * 이메일 인증 번호 생성 및 저장 메서드
   * @param userEmail 인증 번호를 저장하려 식별할 이메일
   */
  //@Cacheable(value = "emailAuth", key = "#userEmail")
  public String generateAndCachePassword(String userEmail) {
    return UUID.randomUUID().toString();
    //log.info(String.valueOf(String.valueOf(redisCacheManager.getCache("emailAuth")).isEmpty()));
  }

  
  /**
   * 중복된 이메일로 가입한 회원이 있는지 여부 체크 메서드
   * @param userEmail 수신할 이메일 주소
   */
  private void isExistUsername(String userEmail) {
    if (userRepository.existsByUsername(userEmail)) {
      throw new IllegalArgumentException("중복된 이메일로 가입한 내역이 존재합니다.");
    }
  }

  /**
   * 이메일 제목 밑 내용을 설정하는 메서드
   * @param userEmail 인증할 이메일 주소
   * @param password 인증번호
   * @return MimeMessagePreparator 타입의 객체
   * @throws Exception
   */
  @Async
  public MimeMessagePreparator setEmailText(String userEmail, String password) throws Exception {
    // 이메일 내용 설정
    return mimeMessage -> {
      MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      messageHelper.setTo(userEmail);
      messageHelper.setSubject("[코끼리] " + " Email 인증을 진행해주세요. ");
      messageHelper.setText(setMailMessage(userEmail,password),true);
    };
/*
    try {
      javaMailSender.send(messagePreparator);
      return ResponseEntity.ok("이메일 전송 완료");
    } catch (MailException e) {
      e.printStackTrace();
      throw new Exception("메일 보내는 도중 오류 발생");
    }*/
  }

  /**
   * 이메일 내용 설정하는 메서드
   * @param sendTo 인증 절차를 밟을 이메일 주소
   * @param password 인증 번호
   * @return
   */
  public String setMailMessage(String sendTo, String password) {
    String msg = "";
    msg += "<html><body style='font-family: Arial, sans-serif; background-color: #ffffff; margin: 0; padding: 0; text-align: center;'>";
    msg += "<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr><td>";
    msg += "<table class='container' width='1000' cellpadding='0' cellspacing='0' border='0' align='center'>";
    msg += "<tr><td style='background-color: #f5f5f5; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);'>";
    msg += "<h1 style='color: #007bff; font-size: 30px;'>코끼리</h1>";
    msg += "<p style='font-size: 16px; margin-top: 20px;'>안녕하세요, " + sendTo
        + "님. 이메일 인증번호 입니다!</p>";
    msg += "<p style='font-size: 16px; margin-top: 20px;'>" + "인증번호: " + password;
    msg += "</td></tr></table></td></tr></table></body></html>";

    return msg;
  }


}
