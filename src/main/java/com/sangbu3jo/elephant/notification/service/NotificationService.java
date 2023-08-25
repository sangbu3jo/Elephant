package com.sangbu3jo.elephant.notification.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sangbu3jo.elephant.notification.entity.Notification;
import com.sangbu3jo.elephant.notification.repository.NotificationRepository;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 사용자 ID를 키로 가지는 SSEEmitter 저장소
    private final Map<Long, List<SseEmitter>> sseEmitterMap = new ConcurrentHashMap<>();

    public void addSseEmitter(Long userId, SseEmitter emitter) {
        sseEmitterMap.computeIfAbsent(userId, key -> new ArrayList<>()).add(emitter);
    }

    public void removeSseEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = sseEmitterMap.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
        }
    }

    public void sendNotificationToUser(Long userId, Notification notification) {
        List<SseEmitter> emitters = sseEmitterMap.get(userId);
        if (emitters != null) {
            emitters.forEach(emitter -> {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule()); // Java 8 날짜/시간 모듈 등록
                    String notificationJson = objectMapper.writeValueAsString(notification);
                    emitter.send(SseEmitter.event().data(notificationJson));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    // 댓글 작성 시 알림 생성 및 전송
    public void createAndSendNotification(Long userId, Long postId, String commentAuthor, String postTitle) {
        Notification notification = new Notification();
        LocalDateTime currentTime = LocalDateTime.now(); // 현재 시간 가져오기
        notification.setCreatedAt(currentTime); // 생성 시간 설정
        notification.setUserId(userId);
        notification.setPostId(postId);
        notification.setContent("\uD83D\uDD14댓글알림\uD83D\uDD14<br>" + commentAuthor);
        notification.setUrl("/post/" + postId);
        notification.setRead(false);

        notificationRepository.save(notification);
        sendNotificationToUser(userId, notification);
    }

    // 데이터에서 알림정보 추출
    public List<Notification> getNotificationList(Long userId) {
        return notificationRepository.findAllByUserId(userId);
    }

    // 알림 단건 읽음처리
    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    // 알림 전체 읽음처리
    @Transactional
    public void markAllNotificationsAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findAllByUserIdAndIsRead(userId, false);
        notifications.forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }


    public User getUserInfo(User requestuser) {
        User user = userRepository.findById(requestuser.getId()).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 사용자 입니다.")
        );

        return user;
    }



}
