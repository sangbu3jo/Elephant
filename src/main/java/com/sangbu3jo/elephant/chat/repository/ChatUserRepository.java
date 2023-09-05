package com.sangbu3jo.elephant.chat.repository;

import com.sangbu3jo.elephant.chat.entity.ChatRoom;
import com.sangbu3jo.elephant.chat.entity.ChatUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    Optional<ChatUser> findByUsernameAndChatroom(String username, ChatRoom chatRoom);

    List<ChatUser> findAllByChatroom(ChatRoom chatRoom);
}
