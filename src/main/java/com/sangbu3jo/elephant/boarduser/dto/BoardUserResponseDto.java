package com.sangbu3jo.elephant.boarduser.dto;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.boarduser.entity.BoardUser;
import com.sangbu3jo.elephant.users.entity.User;
import lombok.Getter;

@Getter
public class BoardUserResponseDto {

    private String username;
    private String nickname;
    private Boolean selected;

    public BoardUserResponseDto(String username, String nickname) {
        this.username = username;
        this.nickname = nickname;
    }

    public BoardUserResponseDto(BoardUser boardUser) {
        this.username = boardUser.getUser().getUsername();
        this.nickname = boardUser.getUser().getNickname();
    }

    public BoardUserResponseDto(User user, Boolean selected) {
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.selected = selected;
    }

}
