package com.sangbu3jo.elephant.cardcomment.dto;


import com.sangbu3jo.elephant.card.entity.Card;
import com.sangbu3jo.elephant.cardcomment.entity.CardComment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CardcommentResponseDto {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String username;
    private String url;

    public CardcommentResponseDto(CardComment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.username = comment.getUser().getUsername();
        this.url = comment.getUser().getProfileUrl();
    }

}
