package com.sangbu3jo.elephant.users.dto;

import com.sangbu3jo.elephant.users.entity.User;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String introduction;
    private String profileUrl;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.nickname = user.getNickname();
        this.introduction = user.getIntroduction();
        this.profileUrl = user.getProfileUrl();
    }
}
