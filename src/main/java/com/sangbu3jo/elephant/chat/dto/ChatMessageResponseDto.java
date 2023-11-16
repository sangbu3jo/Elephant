package com.sangbu3jo.elephant.chat.dto;

import com.sangbu3jo.elephant.chat.entity.ChatMessage;
import com.sangbu3jo.elephant.users.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class ChatMessageResponseDto {

    private String type; // 메시지 타입
    private Long chatRoomId;
    private String nickname; // 클라이언트에서 표시용
    private String username; // 서버와 통신용
    private String message;
    private String sendTime;
    private String url;

    public ChatMessageResponseDto(ChatMessage chatMessage, User user) {
        this.type = chatMessage.getType();
        this.chatRoomId = chatMessage.getChatRoomId();
        this.nickname = chatMessage.getNickname();
        this.username = chatMessage.getUsername();
        this.message = chatMessage.getMessage();
        this.url = user.getProfileUrl();
        this.sendTime = chatMessage.getSendTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")).toString();
    }

    public void updateUrl(String url) {
        this.url = url;
    }

}
