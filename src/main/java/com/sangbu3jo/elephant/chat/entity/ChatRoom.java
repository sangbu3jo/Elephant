package com.sangbu3jo.elephant.chat.entity;

import com.sangbu3jo.elephant.board.entity.Board;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;

    @OneToOne(mappedBy = "chatRoom")
    private Board board;

    @OneToMany(mappedBy = "chatroom", orphanRemoval = true, cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<ChatUser> users = new ArrayList<>();

    public ChatRoom(Long id, Board board){
        this.roomId = id;
        this.board = board;
    }

    public void addUser(ChatUser chatUser) {
        this.users.add(chatUser);
        chatUser.updateChatRoom(this);
    }


}
