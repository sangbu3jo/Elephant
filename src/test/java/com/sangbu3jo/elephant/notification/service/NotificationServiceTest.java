package com.sangbu3jo.elephant.notification.service;

import com.sangbu3jo.elephant.notification.entity.Notification;
import com.sangbu3jo.elephant.notification.repository.NotificationRepository;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this);

        // UserService 초기화
        notificationService = new NotificationService(notificationRepository, userRepository);
    }

    @Test
    @DisplayName("알림 추가")
    void addSseEmitter() {
        // given
        Long userId = 1L;
        SseEmitter emitter = new SseEmitter();

        // when
        notificationService.addSseEmitter(userId, emitter);

        // then
        assertTrue(notificationService.getSseEmitterMap().containsKey(userId));
        assertEquals(1, notificationService.getSseEmitterMap().get(userId).size());
    }

    @Test
    @DisplayName("알림 제거")
    void removeSseEmitter() {
        // given
        Long userId = 1L;
        SseEmitter emitter = new SseEmitter();
        notificationService.addSseEmitter(userId, emitter);

        // when
        notificationService.removeSseEmitter(userId, emitter);

        // then
        assertFalse(notificationService.getSseEmitterMap().get(userId).contains(emitter));
    }

    @Test
    @DisplayName("알림 보내기")
    void sendNotificationToUser() {
        // given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        SseEmitter emitter = new SseEmitter();

        Notification notification = new Notification();
        notification.setContent("새로운 알림");

        NotificationService notificationService = Mockito.mock(NotificationService.class);
//        Mockito.when;

        // when
        notificationService.addSseEmitter(userId, emitter);
        notificationService.sendNotificationToUser(userId, notification);

        // then
        // ?????????????
    }

    @Test
    @DisplayName("댓글 작성 시 알림 생성 및 전송")
    void createAndSendNotification() {
        // given
        Long userId = 1L;
        Long postId = 2L;
        String commentUser = "hello";
        String postTitle = "post";

        // when
        notificationService.createAndSendNotification(userId, postId, commentUser, postTitle);

        // then
        // ??

    }

    @Test
    @DisplayName("프로젝트에 유저 참여시 알림 생성 및 전송")
    void addUserAndSendNotification() {
        // given
        Long userId = 1L;
        Long boardId = 2L;
        String addUser = "gunwook";

        // when
        notificationService.addUserAndSendNotification(userId, boardId, addUser);

        // then
        // ??

    }

    @Test
    @DisplayName("프로젝트 초대시 알림 생성 및 전송")
    void inviteAndSendNotification() {
        // given
        Long userId = 1L;
        String inviteUser = "gunwook";

        // when
        notificationService.inviteAndSendNotification(userId, inviteUser);

        // then
        // ??

    }

    @Test
    @DisplayName("데이터에서 알림정보 추출")
    void getNotificationList() {
        // given
        Long userId = 1L;
        Notification notification1 = new Notification();
        notification1.setContent("hello");
        Notification notification2 = new Notification();
        notification2.setContent("hihi");

        List<Notification> notifications = new ArrayList<>();
        notifications.add(notification1);
        notifications.add(notification2);

        when(notificationRepository.findAllByUserId(userId)).thenReturn(notifications);

        // when
        List<Notification> result = notificationService.getNotificationList(userId);

        // then
        assertEquals(notifications.size(), result.size());
        assertEquals(notification1.getContent(), result.get(0).getContent());
        assertEquals(notification2.getContent(), result.get(1).getContent());

    }

    @Test
    @DisplayName("알림 단건 읽음처리")
    void markNotificationAsRead() {
        // given
        Long notificationId = 1L;
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(java.util.Optional.of(notification));

        // when
        notificationService.markNotificationAsRead(notificationId);

        // then
        assertTrue(notification.isRead());
    }

    @Test
    @DisplayName("알림 전체 읽음처리")
    void markAllNotificationsAsRead() {
        // given
        Long userId = 1L;
        Notification notification1 = new Notification();
        notification1.setId(1L);
        notification1.setUserId(userId);
        notification1.setRead(false);
        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setUserId(userId);
        notification2.setRead(false);

        List<Notification> notifications = new ArrayList<>();
        notifications.add(notification1);
        notifications.add(notification2);

        when(notificationRepository.findAllByUserIdAndIsRead(userId, false)).thenReturn(notifications);

        // when
        notificationService.markAllNotificationsAsRead(userId);

        // then
        assertTrue(notification1.isRead());
        assertTrue(notification2.isRead());
    }

    @Test
    @DisplayName("유저 정보 조회")
    void getUserInfo() {
        // given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setNickname("gunwook");

        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        User result = notificationService.getUserInfo(user);

        // then
        assertEquals("gunwook", result.getNickname());
    }
}