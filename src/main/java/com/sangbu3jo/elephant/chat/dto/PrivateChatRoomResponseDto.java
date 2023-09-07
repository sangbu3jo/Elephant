package com.sangbu3jo.elephant.chat.dto;


import com.sangbu3jo.elephant.chat.entity.GroupChatRoom;
import com.sangbu3jo.elephant.chat.entity.GroupChatUser;
import com.sangbu3jo.elephant.chat.entity.PrivateChatMessage;
import com.sangbu3jo.elephant.chat.entity.PrivateChatRoom;
import com.sangbu3jo.elephant.chat.repository.PrivateChatRoomRepository;
import lombok.Getter;
import org.springframework.cglib.core.Local;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PrivateChatRoomResponseDto {

    private String title;
    private String showUser;
    private String message;
    private String date;
    private LocalDateTime time;

    public PrivateChatRoomResponseDto(PrivateChatRoom privateChatRoom, String username, PrivateChatMessage privateChatMessage) {
        this.title = privateChatRoom.getTitle();
        // username과 동일하면 다른 사람을 showUser에 담아서 반환하도록 함
        this.showUser = setPrivateChatTitle(username, privateChatRoom);

        // 최근 메세지 내역이 있으면 그 내용을 보여줌
        if (privateChatMessage != null) {
            this.time = privateChatMessage.getSendTime();

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
        if (privateChatMessage != null) {
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

    public PrivateChatRoomResponseDto(GroupChatRoom groupChatRoom, String username, PrivateChatMessage privateChatMessage) {
        this.title = groupChatRoom.getTitle();
        // username과 동일하면 다른 사람을 showUser에 담아서 반환하도록 함
        this.showUser = setChatTitle(username, groupChatRoom);

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
        if (privateChatMessage != null) {
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

    public String setChatTitle(String username, GroupChatRoom groupChatRoom) {
        String title = "";
        List<GroupChatUser> users = groupChatRoom.getGroupChatUsers();

        for (int i = 0; i < users.size(); i++) {
            GroupChatUser g = users.get(i);
            if (g.getUser().getUsername().equals(username)) {
                continue;
            }

            if (!title.isEmpty()) {
                title += ", ";
            }

            title += g.getUser().getUsername();

            if (title.length() > 70) {
                break;
            }
        }

        if (title.length() > 70) {
            return title.substring(0, 70) + "...";
        } else {
            return title;
        }
    }

    public String setPrivateChatTitle(String username, PrivateChatRoom privateChatRoom) {
        if (privateChatRoom.getUser1().equals(username)) {
            return privateChatRoom.getUser2();
        } else {
            return privateChatRoom.getUser1();
        }
    }


}
