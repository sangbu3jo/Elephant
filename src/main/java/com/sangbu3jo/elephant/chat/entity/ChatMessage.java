package com.sangbu3jo.elephant.chat.entity;

import com.sangbu3jo.elephant.chat.dto.ChatMessageRequestDto;
import com.sangbu3jo.elephant.chat.dto.MessageType;

import java.time.LocalDateTime;


// mongodb에 저장할 내용
public class ChatMessage {

    private MessageType type; // 메시지 타입
    private Long chatRoomId;
    private String nickname; // 클라이언트에서 표시용
    private String username; // 서버와 통신용
    private String message;
    private LocalDateTime sendTime;

    public ChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        this.chatRoomId = chatMessageRequestDto.getChatRoomId();
        this.nickname = chatMessageRequestDto.getNickname();
        this.username = chatMessageRequestDto.getUsername();
        this.message = chatMessageRequestDto.getMessage();
        this.sendTime = chatMessageRequestDto.getSendTime();
        this.type = chatMessageRequestDto.getType();
    }


}
