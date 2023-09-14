package com.sangbu3jo.elephant.chat.dto;

import com.sangbu3jo.elephant.chat.entity.PrivateChatMessage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class PrivateChatMessageResponseDto {

    private String type; // 메시지 타입
    private String title;
    private String nickname; // 클라이언트에서 표시용
    private String username; // 서버와 통신용
    private String message;
    private String sendTime;
    private String url;

    public PrivateChatMessageResponseDto (PrivateChatMessage privateChatMessage) {
        this.type = privateChatMessage.getType();
        this.title = privateChatMessage.getTitle();
        this.nickname = privateChatMessage.getUser().getNickname();
        this.username = privateChatMessage.getUser().getUsername();
        this.message = privateChatMessage.getMessage();
        this.sendTime = privateChatMessage.getSendTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")).toString();
    }

    public void updateUrl(String url) {
        this.url = url;
    }

}
