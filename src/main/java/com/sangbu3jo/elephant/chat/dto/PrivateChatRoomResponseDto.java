package com.sangbu3jo.elephant.chat.dto;


import com.sangbu3jo.elephant.chat.entity.PrivateChatMessage;
import com.sangbu3jo.elephant.chat.entity.PrivateChatRoom;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class PrivateChatRoomResponseDto {

    private String title;
    private String showUser;
    private String message;
    private String date;

    public PrivateChatRoomResponseDto(PrivateChatRoom privateChatRoom, String username, PrivateChatMessage privateChatMessage) {
        this.title = privateChatRoom.getTitle();
        // username과 동일하면 다른 사람을 showUser에 담아서 반환하도록 함
        if (privateChatRoom.getFirstUser().equals(username)) {
            this.showUser =  privateChatRoom.getSecondUser();
        } else {
            this.showUser =  privateChatRoom.getFirstUser();
        }
        // 최근 메세지 내역이 있으면 그 내용을 보여줌
        if (privateChatMessage != null) {
            String message = privateChatMessage.getMessage();
            if (message.length() > 20) {
                this.message = message.substring(0, 20) + "...";
            } else {
                this.message = message;
            }
        } else {
            this.message = "채팅을 시작해 보세요 !";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(privateChatMessage.getSendTime(), now);
        long seconds = duration.getSeconds();
        if (seconds >= 24 * 3600) {
            long days = seconds / (24 * 3600);
            this.date =  days + "일 전";
        } else if (seconds >= 3600) {
            long hours = seconds / 3600;
            this.date = hours + "시간 전";
        } else if (seconds >= 60) {
            long minutes = seconds / 60;
            this.date = minutes + "분 전";
        } else {
            this.date = seconds + "초 전";
        }
    }


}
