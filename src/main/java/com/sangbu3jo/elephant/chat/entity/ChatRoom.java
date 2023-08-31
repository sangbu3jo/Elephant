package com.sangbu3jo.elephant.chat.entity;

import com.sangbu3jo.elephant.board.entity.Board;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
    private Long id;

    @OneToOne
    private Board board;

    @OneToMany(mappedBy = "chatroom", orphanRemoval = true)
    private List<ChatUser> users = new ArrayList<>();

    public ChatRoom(Long id, Board board){
        this.id = id;
        this.board = board;
    }

    public void addUser(ChatUser chatUser) {
        this.users.add(chatUser);
        chatUser.updateChatRoom(this);
    }


}
