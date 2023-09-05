package com.sangbu3jo.elephant.chat.repository;

import com.sangbu3jo.elephant.chat.entity.GroupChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {
    GroupChatRoom findByTitle(String chatRoomId);
}
