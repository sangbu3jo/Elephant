package com.sangbu3jo.elephant.board.dto;


import com.sangbu3jo.elephant.board.entity.Board;
import lombok.Getter;

import java.time.LocalDate;
import java.time.Period;

@Getter
public class BoardResponseDto {

    private Long boardid;
    private String title;
    private String content;
    private LocalDate expiredAt;
    private String dday;

    public BoardResponseDto(Board board) {
        this.boardid = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.expiredAt = board.getExpiredAt();
        this.dday = calculateRemainingDays(board.getExpiredAt());
    }

    public String calculateRemainingDays(LocalDate expiredAt) {
        LocalDate today = LocalDate.now();
        Period period = Period.between(today, expiredAt);

        if (period.isNegative()) {
            return "마감완료";
        } else if (period.isZero()) {
            return "D-Day";
        } else {
            return "D-" + period.getDays();
        }
    }

}
