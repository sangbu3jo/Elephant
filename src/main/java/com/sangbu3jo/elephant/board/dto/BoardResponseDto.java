package com.sangbu3jo.elephant.board.dto;


import com.sangbu3jo.elephant.board.entity.Board;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class BoardResponseDto {

    private String title;
    private String content;
    private LocalDate expiredAt;

    public BoardResponseDto(Board board) {
        this.title = board.getTitle();
        this.content = board.getContent();
        this.expiredAt = board.getExpiredAt();
    }

}
