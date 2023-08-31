package com.sangbu3jo.elephant.card.entity;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.card.dto.CardRequestDto;
import com.sangbu3jo.elephant.cardcomment.entity.CardComment;
import com.sangbu3jo.elephant.carduser.entity.CardUser;
import com.sangbu3jo.elephant.columns.entity.Columns;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// lombok
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// jpa
@Entity
@Table(name = "card")
public class Card {

    /**
     * 컬럼 - 연관관계 컬럼을 제외한 컬럼을 정의합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "expired_at")
    private LocalDate expiredAt;

    @Column(name = "card_order")
    private Long cardOrder;

    /**
     * 생성자 - 약속된 형태로만 생성가능하도록 합니다.
     */
    public Card(CardRequestDto cardRequestDto, Columns columns, Integer order) {
        this.title = cardRequestDto.getTitle();
        this.content = cardRequestDto.getContent();
        this.expiredAt = cardRequestDto.getExpiredAt();
        this.columns = columns;
        this.cardOrder = Long.valueOf(order);
        this.board = columns.getBoard();
    }

    /**
     * 연관관계 - Foreign Key 값을 따로 컬럼으로 정의하지 않고 연관 관계로 정의합니다.
     */
    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne
    @JoinColumn(name = "column_id")
    private Columns columns;

    @OneToMany(mappedBy = "card", orphanRemoval = true)
    List<CardComment> cardComments = new LinkedList<>();

    @OneToMany(mappedBy = "card", orphanRemoval = true)
    List<CardUser> cardUsers = new ArrayList<>();

    /**
     * 연관관계 편의 메소드 - 반대쪽에는 연관관계 편의 메소드가 없도록 주의합니다.
     */


    /**
     * 서비스 메소드 - 외부에서 엔티티를 수정할 메소드를 정의합니다. (단일 책임을 가지도록 주의합니다.)
     */
    public void updateCard(CardRequestDto cardRequestDto) {
        if (cardRequestDto.getTitle() != null) {
            this.title = cardRequestDto.getTitle();
        }
        if (cardRequestDto.getContent() != null) {
            this.content = cardRequestDto.getContent();
        }
        if (cardRequestDto.getExpiredAt() != null) {
            this.expiredAt = cardRequestDto.getExpiredAt();
        }
    }

    public void addCardcomment(CardComment comment) {
        this.cardComments.add(comment);
    }

    public void removeCardcomment(CardComment comment) {
        this.cardComments.remove(comment);
    }

    public void updateCardOrder(Long order) {
        this.cardOrder = order;
    }

    public void setColumn(Columns newcolumn) {
        this.columns = newcolumn;
    }

    public void addCardUser(CardUser cardUser) {
        this.cardUsers.add(cardUser);
    }
}
