package com.sangbu3jo.elephant.chat.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PrivateChatMessageRequestDto {

    private String title;
    private String message;
    private String username;
    private String nickname;
    private LocalDateTime sendTime;
    private MessageType type;

    public void setMessage (String message) {
        this.message = message;
    }

    public void setSendTime (LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public PrivateChatMessageRequestDto(MessageType type, String username, String nickname, String title, String message) {
        this.username = username;
        this.nickname = nickname;
        this.type = type;
        this.title = title;
        this.message = message;
        this.sendTime = LocalDateTime.now();
    }

}
