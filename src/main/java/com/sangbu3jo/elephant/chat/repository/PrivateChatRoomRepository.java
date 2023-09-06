package com.sangbu3jo.elephant.chat.repository;

import com.sangbu3jo.elephant.chat.entity.PrivateChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrivateChatRoomRepository extends JpaRepository<PrivateChatRoom, Long> {

    @Query("SELECT p FROM PrivateChatRoom p WHERE (p.user1 = :user1 AND p.user2 = :user2) OR (p.user1 = :user2 AND p.user2 = :user1)")
    Optional<PrivateChatRoom> findByUser1AndUser2(@Param("user1") String user1, @Param("user2") String user2);

    @Query("SELECT p FROM PrivateChatRoom p WHERE (p.user1 = :username) OR (p.user2 = :username)")
    List<PrivateChatRoom> findByUser(String username);

    Optional<PrivateChatRoom> findByTitle(String chatRoomId);
}
