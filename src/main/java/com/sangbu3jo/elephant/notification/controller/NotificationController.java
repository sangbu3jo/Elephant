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
    @GetMapping("/notifications/{userId}")
    public SseEmitter getNotifications(@PathVariable Long userId) {
        SseEmitter emitter = new SseEmitter();

        // 알림을 수신하는 SSE 연결 등록
        notificationService.addSseEmitter(userId, emitter);

        emitter.onTimeout(() -> notificationService.removeSseEmitter(userId, emitter));
        emitter.onCompletion(() -> notificationService.removeSseEmitter(userId, emitter));

        return emitter;
    }

    // 데이터에서 알림정보 추출
    @GetMapping("/notifications/list/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsList(@PathVariable Long userId){
        List<Notification> Notifications = notificationService.getNotificationList(userId);
        return ResponseEntity.ok(Notifications);
    }



    // 알림 단건 읽음처리
    @PostMapping("/mark-notification-as-read/{notificationId}")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markNotificationAsRead(notificationId);
            return new ResponseEntity<>("알림을 확인하였습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("알림을 확인하는데 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 알림 전체 읽음처리
    @PostMapping("/mark-all-notifications-as-read/{userId}")
    public ResponseEntity<String> markAllNotificationsAsRead(@PathVariable Long userId) {
        try {
            notificationService.markAllNotificationsAsRead(userId);
            return new ResponseEntity<>("모든 알림을 확인하였습니다", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("모든 알림을 확인하는데 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-user-info")
    public ResponseEntity<User> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails){
        User user = notificationService.getUserInfo(userDetails.getUser());
        return ResponseEntity.ok(user);
    }

}
