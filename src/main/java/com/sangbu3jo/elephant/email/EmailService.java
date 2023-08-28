package com.sangbu3jo.elephant.email;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
import com.sangbu3jo.elephant.boarduser.entity.BoardUser;
import com.sangbu3jo.elephant.boarduser.repository.BoardUserRepository;
import com.sangbu3jo.elephant.notification.service.NotificationService;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine springTemplateEngine;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final BoardUserRepository boardUserRepository;
    private final NotificationService notificationService;

    public ResponseEntity<String> confirming(Long boardId, EmailUserDto emailUserDto, User user) {
        // user는 초대하는 사람 (user)
        // username은 초대 받는 사람
        // boardId는 초대할 보드의 id
        List<User> users = new ArrayList<>();
        try {
            Board board = findBoard(boardId);
            for (String username : emailUserDto.getInviteUsers()) {
                User invitedUser = findUser(username);
                try {
                    findBoardUser(board, invitedUser);
                } catch (IllegalArgumentException e) {
                    users.add(invitedUser);
                }
            }
            for (User user1: users) {
                try {
                    sendEmail(board, user1.getUsername(), user);
                    // 초대받은 사용자에게 알람 보내기
                    String notificationContent = user.getNickname() + "님이 회원님을 \"" + board.getTitle() + "\" 프로젝트에 초대하셨습니다. 가입하신 이메일을 확인해주세요.";
                    notificationService.inviteAndSendNotification(user1.getId(), notificationContent);

                } catch (Exception e) {
//                    return ResponseEntity.badRequest().body("사용자 초대 실패");
                }
            }

            return ResponseEntity.ok().body("사용자 초대 완료!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("가입되어 있지 않은 사용자는 초대할 수 없습니다.");
        }
    }

    @Async
    public void sendEmail(Board board, String invitedUser, User invitingUser) throws Exception {
        // EmailDto 내용 설정 (이메일을 받을 사람, 이메일의 제목, 이메일을 보내는 사람)
        EmailDto emailDto = EmailDto.builder()
                .sendTo(invitedUser)
                .sendFrom(invitingUser.getNickname())
                .subject("[코끼리] " + invitingUser.getNickname() + "님이 " + board.getTitle() + "프로젝트에 초대하셨습니다.")
                .build();

        // 이메일 내용 설정
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setTo(emailDto.getSendTo());
            messageHelper.setSubject(emailDto.getSubject());
            messageHelper.setText(setMailMessage(emailDto.getSendTo(), invitingUser.getUsername(), board), true);
        };

        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            e.printStackTrace();
            throw new Exception("메일 보내는 도중 오류 발생");
        }
    }

    public String setMailMessage(String sendTo, String sendFrom, Board board) throws UnsupportedEncodingException {
        String msg = "";
        msg += "<h1 style=\"font-size: 30px; padding-right: 30px; padding-left: 30px;\">코끼리</h1>";
        msg += "<p style=\"font-size: 16px; padding-right: 30px; padding-left: 30px;\">";
        msg += "안녕하세요 " + sendTo + "님. 코끼리입니다 !" + "<br>";
        msg += sendFrom + "님 으로부터 " + board.getTitle() + "프로젝트에 초대되셨습니다! </p>";
        msg += "<div style=\"padding-right: 30px; padding-left: 30px; margin: 32px 0 40px;\"><table style=\"border-collapse: collapse; border: 0; background-color: #F4F4F4; height: 70px; table-layout: fixed; word-wrap: break-word; border-radius: 6px;\"><tbody><tr><td style=\"text-align: center; vertical-align: middle; font-size: 30px;\">";
        msg += "<a href=\"http://localhost:8080/api/boards/" + board.getId() + "/member?member=" + URLEncoder.encode(sendTo, "UTF-8") + "\">" + "프로젝트에 참여하기" + "</a>";
        msg += "</td></tr></tbody></table></div>";
        return msg;
    }

    public User findUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(IllegalArgumentException::new);
    }

    public Board findBoard(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(IllegalArgumentException::new);
    }


    public BoardUser findBoardUser(Board board, User user) {
        return boardUserRepository.findByBoardAndUser(board, user).orElseThrow(IllegalArgumentException::new);
    }


}
