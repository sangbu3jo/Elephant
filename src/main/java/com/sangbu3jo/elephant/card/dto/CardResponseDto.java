package com.sangbu3jo.elephant.card.dto;

import com.sangbu3jo.elephant.card.entity.Card;
import lombok.Getter;

import java.time.LocalDate;
import java.time.Period;

@Getter
public class CardResponseDto {

    private Long cardid;
    private String title;
    private Long cardOrder;
    private String dday;

    public CardResponseDto(Card card) {
        this.cardid = card.getId();
        this.title = card.getTitle();
        this.cardOrder = card.getCardOrder();
        this.dday = remainingDays(card.getExpiredAt());
    }

    public String remainingDays(LocalDate expiredAt) {
        if (expiredAt != null) {
            LocalDate today = LocalDate.now();
            Period period = Period.between(today, expiredAt);

            if (period.isNegative()) {
                return "종료";
            } else if (period.isZero()) {
                return "D-Day";
            } else {
                return "D-" + period.getDays();
            }
        } else {
            return "";
        }

    }

}
