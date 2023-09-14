package com.sangbu3jo.elephant.chat.dto;

import com.sangbu3jo.elephant.chat.entity.GroupChatUser;
import com.sangbu3jo.elephant.users.entity.User;
import lombok.Getter;

@Getter
public class GroupChatUserResponseDto {

    String url;
    String username;
    String nickname;

    public GroupChatUserResponseDto(GroupChatUser groupChatUser) {
        this.url = groupChatUser.getUser().getProfileUrl();
        this.username = groupChatUser.getUser().getUsername();
        this.nickname = groupChatUser.getUser().getNickname();
    }

}
