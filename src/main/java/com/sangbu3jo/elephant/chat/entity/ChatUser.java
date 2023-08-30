package com.sangbu3jo.elephant.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name="chat_user")
public class ChatUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private LocalDateTime enterTime;
    @ManyToOne
    private ChatRoom chatroom;

    public ChatUser(String username, LocalDateTime time) {
        this.username = username;
        this.enterTime = time;
    }

    public ChatUser() {

    }

    public void updateChatRoom(ChatRoom chatRoom) {
        this.chatroom = chatRoom;
    }

}
