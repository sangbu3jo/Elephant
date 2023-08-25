package com.sangbu3jo.elephant.chat.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatMessageRequestDto {

    private MessageType type; // 메시지 타입

    private Long chatRoomId;
    private String nickname; // 클라이언트에서 표시용
    private String username; // 서버와 통신용
    private String message;
    private LocalDateTime sendTime;

    public void setMessage (String message) {
        this.message = message;
    }

    public ChatMessageRequestDto (String nickname, String username, MessageType type, Long chatRoomId) {
        this.nickname = nickname;
        this.username = username;
        this.type = type;
        this.chatRoomId = chatRoomId;
        this.sendTime = LocalDateTime.now();
    }

}