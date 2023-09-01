package com.sangbu3jo.elephant.notification.controller;

import com.sangbu3jo.elephant.notification.entity.Notification;
import com.sangbu3jo.elephant.notification.service.NotificationService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NotificationController {
    private final NotificationService notificationService;


    /**
     * 특정 사용자의 알림을 받기 위한 SSE (Server-Sent Events) 연결을 생성합니다.
     *
     * @param userId 알림을 받을 사용자의 식별자
     * @return SSE 연결 객체 (SseEmitter)
     */
    @GetMapping("/notifications/{userId}")
    public SseEmitter getNotifications(@PathVariable Long userId) {
        SseEmitter emitter = new SseEmitter();

        // 알림을 수신하는 SSE 연결 등록
        notificationService.addSseEmitter(userId, emitter);

        emitter.onTimeout(() -> notificationService.removeSseEmitter(userId, emitter));
        emitter.onCompletion(() -> notificationService.removeSseEmitter(userId, emitter));

        return emitter;
    }

    /**
     * 특정 사용자의 알림 목록을 가져옵니다.
     *
     * @param userId 알림 목록을 가져올 사용자의 식별자
     * @return 알림 목록을 담은 ResponseEntity
     */
    @GetMapping("/notifications/list/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsList(@PathVariable Long userId){
        List<Notification> Notifications = notificationService.getNotificationList(userId);
        return ResponseEntity.ok(Notifications);
    }



    /**
     * 특정 알림을 읽음으로 표시합니다.
     *
     * @param notificationId 읽음으로 표시할 알림의 식별자
     * @return 읽음으로 표시 성공 또는 실패에 대한 ResponseEntity
     */
    @PostMapping("/mark-notification-as-read/{notificationId}")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markNotificationAsRead(notificationId);
            return new ResponseEntity<>("알림을 확인하였습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("알림을 확인하는데 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 사용자의 모든 알림을 읽음으로 표시합니다.
     *
     * @param userId 모든 알림을 읽음으로 표시할 사용자의 식별자
     * @return 읽음으로 표시 성공 또는 실패에 대한 ResponseEntity
     */
    @PostMapping("/mark-all-notifications-as-read/{userId}")
    public ResponseEntity<String> markAllNotificationsAsRead(@PathVariable Long userId) {
        try {
            notificationService.markAllNotificationsAsRead(userId);
            return new ResponseEntity<>("모든 알림을 확인하였습니다", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("모든 알림을 확인하는데 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 현재 인증된 사용자의 정보를 가져옵니다.
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @return 현재 인증된 사용자의 정보 또는 오류 메시지를 담은 ResponseEntity
     */
    @GetMapping("/get-user-info")
    public ResponseEntity<Object> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails){
        if(userDetails!=null) {
            User user = notificationService.getUserInfo(userDetails.getUser());
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest().body("유저 정보가 없습니다.");
    }

}
