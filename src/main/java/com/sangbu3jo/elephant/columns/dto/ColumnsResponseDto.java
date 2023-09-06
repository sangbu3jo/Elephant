package com.sangbu3jo.elephant.columns.dto;

import com.sangbu3jo.elephant.card.dto.CardResponseDto;
import com.sangbu3jo.elephant.columns.entity.Columns;
import lombok.Getter;

import java.util.List;

@Getter
public class ColumnsResponseDto {

    private Long columnid;
    private String title;
    private Long order;
    private List<CardResponseDto> cardResponseDtos;

    public ColumnsResponseDto(Columns columns) {
        this.columnid = columns.getId();
        this.title = columns.getTitle();
        this.order = columns.getColumnOrder();
    }

    public void updateCardList(List<CardResponseDto> cardResponseDtos) {
        this.cardResponseDtos = cardResponseDtos;
    }

}
