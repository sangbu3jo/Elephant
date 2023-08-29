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
    private Long id;

    @OneToOne
    private Board board;

    @ElementCollection(fetch = FetchType.EAGER) // 다른 방법을 생각해보자
    private List<String> users = new ArrayList<>();

    public ChatRoom(Long id, Board board){
        this.id = id;
        this.board = board;
    }

    public void addUser(String username) {
        this.users.add(username);
    }
//    public void removeUser(String username) {
//        this.users.remove(username);
//    }

}
