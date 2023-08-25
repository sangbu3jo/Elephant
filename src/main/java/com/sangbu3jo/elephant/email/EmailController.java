package com.sangbu3jo.elephant.email;

import com.sangbu3jo.elephant.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    // 사용자에게 초대링크 전송하기
    @PostMapping("/boards/{boardId}/invited")
    public ResponseEntity<String> inviteUser(@PathVariable Long boardId,
                                             @RequestBody EmailUserDto emailUserDto,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception {
        log.info("서버로 요청 넘어옴");
        return emailService.confirming(boardId, emailUserDto, userDetails.getUser());
    }

}
