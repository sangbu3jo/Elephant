package com.sangbu3jo.elephant.email.controller;

import com.sangbu3jo.elephant.email.dto.EmailAuthRequestDto;
import com.sangbu3jo.elephant.email.dto.EmailUserDto;
import com.sangbu3jo.elephant.email.service.EmailCheckService;
import com.sangbu3jo.elephant.email.service.EmailService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final EmailCheckService emailCheckService;

    // 사용자에게 초대링크 전송하기
    @PostMapping("/boards/{boardId}/invited")
    public ResponseEntity<String> inviteUser(@PathVariable Long boardId,
                                             @RequestBody EmailUserDto emailUserDto,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception {
        log.info("서버로 요청 넘어옴");
        return emailService.confirming(boardId, emailUserDto, userDetails.getUser());
    }

  /**
   * 용자에게 이메일 인증을 위한 메일 전송 메서드
   * @param userEmail 수신할 이메일 주소
   * @return 메일 수신에 따른 상태 코드값과 결과값
   */
    @GetMapping("/auth/email/{userEmail}/invited")
    public ResponseEntity<String> sendEmail(@PathVariable String userEmail) throws Exception {
      return emailCheckService.sendEmail(userEmail);
    }

  /**
   * 이메일 인증을 확인하는 메서드
   * @param userEmail 인증 절차를 거칠 이메일 주소
   * @param requestDto 인증번호를 받는 객체
   * @return 인증 결과에 따른 상태 코드값과 결과값
   */
    @PostMapping("/auth/email/{userEmail}/checked")
    public ResponseEntity<String> checkEmail(
        @PathVariable String userEmail,
        @RequestBody EmailAuthRequestDto requestDto) {
        return emailCheckService.checkPassword(userEmail,requestDto.getPassword());
    }


}
