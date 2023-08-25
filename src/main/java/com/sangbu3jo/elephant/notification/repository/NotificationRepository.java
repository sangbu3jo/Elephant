package com.sangbu3jo.elephant.notification.repository;

import com.sangbu3jo.elephant.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserIdAndIsRead(Long userId, boolean isRead);
    List<Notification> findAllByUserId(Long userId);
}
