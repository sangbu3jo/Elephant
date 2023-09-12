package com.sangbu3jo.elephant.chat.repository;

import com.sangbu3jo.elephant.chat.entity.GroupChatRoom;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {
    GroupChatRoom findByTitle(String chatRoomId);
    @Query("SELECT p.id FROM GroupChatRoom p WHERE p.title = :title")
    Long findIdByTitle(@Param("title") String title);
}
