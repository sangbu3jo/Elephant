package com.sangbu3jo.elephant.chat.entity;

import com.sangbu3jo.elephant.board.entity.Board;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

    @Id
    private Long id;

    @OneToOne
    private Board board;

    public ChatRoom(Long id, Board board){
        this.id = id;
        this.board = board;
    }

}
