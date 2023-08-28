package com.sangbu3jo.elephant.chat.dto;

import com.sangbu3jo.elephant.chat.entity.ChatMessage;
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
    private LocalDateTime sendTime;

    public ChatMessageResponseDto(ChatMessage chatMessage) {
        this.type = chatMessage.getType();
        this.chatRoomId = chatMessage.getChatRoomId();
        this.nickname = chatMessage.getNickname();
        this.username = chatMessage.getUsername();
        this.message = chatMessage.getMessage();

        this.sendTime = LocalDateTime.parse(chatMessage.getSendTime().substring(0, 19), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
//        this.sendTime = LocalDateTime.parse(chatMessage.getSendTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

}
