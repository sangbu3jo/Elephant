package com.sangbu3jo.elephant.chat.repository;

import com.sangbu3jo.elephant.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}