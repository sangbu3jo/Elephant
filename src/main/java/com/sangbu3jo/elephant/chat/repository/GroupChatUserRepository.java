package com.sangbu3jo.elephant.chat.repository;

import com.sangbu3jo.elephant.chat.entity.GroupChatRoom;
import com.sangbu3jo.elephant.chat.entity.GroupChatUser;
import com.sangbu3jo.elephant.openai.controller.OpenAIController;
import com.sangbu3jo.elephant.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupChatUserRepository extends JpaRepository<GroupChatUser, Long> {
    List<GroupChatUser> findByUser(User user);

    Optional<GroupChatUser> findByUserAndGroupChatRoom(User user, GroupChatRoom groupChatRoom);
}
