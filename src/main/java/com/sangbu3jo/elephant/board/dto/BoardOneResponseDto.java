package com.sangbu3jo.elephant.board.dto;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.columns.dto.ColumnsResponseDto;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class BoardOneResponseDto {

    private Long id;
    private String title;
    private String content;
    private LocalDate expiredAt;
    private List<ColumnsResponseDto> columnsResponseDtos;

    public BoardOneResponseDto(Board board) {
        this.id  = board.getId();;
        this.title = board.getTitle();
        this.content = board.getContent();
        this.expiredAt = board.getExpiredAt();
        this.columnsResponseDtos = board.getColumnsList().stream().map(ColumnsResponseDto::new).toList();
    }

}
