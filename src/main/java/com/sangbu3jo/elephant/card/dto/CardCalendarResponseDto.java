package com.sangbu3jo.elephant.card.dto;

import com.sangbu3jo.elephant.card.entity.Card;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CardCalendarResponseDto {

    private Long id;
    private String title;
    private LocalDate start;
    private LocalDate end;

    public CardCalendarResponseDto(Card card) {
        if (card.getExpiredAt() != null) {
            this.id = card.getId();
            this.title = card.getTitle();
            this.start = card.getExpiredAt();
            this.end = card.getExpiredAt();
        }
    }

}
