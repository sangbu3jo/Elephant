package com.sangbu3jo.elephant.card.dto;

import com.sangbu3jo.elephant.card.entity.Card;
import com.sangbu3jo.elephant.cardcomment.dto.CardcommentResponseDto;
import com.sangbu3jo.elephant.cardcomment.entity.CardComment;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class CardOneResponseDto {

    private Long boardid;
    private Long cardid;
    private String title;
    private String content;
    private String columntitle;
    private LocalDate expiredAt;
    private Long cardOrder;
    private List<CardcommentResponseDto> cardCommentList;

    public CardOneResponseDto(Card card) {
        this.boardid = card.getColumns().getBoard().getId();
        this.cardid = card.getId();
        this.title = card.getTitle();
        this.columntitle = card.getColumns().getTitle();
        this.content = card.getContent();
        this.expiredAt = card.getExpiredAt();
        this.cardOrder = card.getCardOrder();
        this.cardCommentList = card.getCardComments().stream().map(CardcommentResponseDto::new).toList();
    }
}
