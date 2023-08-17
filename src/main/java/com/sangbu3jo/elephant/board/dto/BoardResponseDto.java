package com.sangbu3jo.elephant.board.dto;


import com.sangbu3jo.elephant.board.entity.Board;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class BoardResponseDto {

    private Long boardid;
    private String title;
    private LocalDate expiredAt;

    public BoardResponseDto(Board board) {
        this.boardid = board.getId();
        this.title = board.getTitle();
        this.expiredAt = board.getExpiredAt();
    }

}
