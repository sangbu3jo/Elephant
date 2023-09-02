package com.sangbu3jo.elephant.columns.entity;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.card.entity.Card;
import com.sangbu3jo.elephant.columns.dto.ColumnsRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

// lombok
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// jpa
@Entity
@Table(name = "columns")
public class Columns {

    /**
     * 컬럼 - 연관관계 컬럼을 제외한 컬럼을 정의합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "column_order")
    private Long columnOrder;


    /**
     * 생성자 - 약속된 형태로만 생성가능하도록 합니다.
     */
    public Columns(ColumnsRequestDto columnsRequestDto, Board board, Integer order) {
        this.title = columnsRequestDto.getTitle();
        this.columnOrder = Long.valueOf(order);
        this.board = board;
    }

    /**
     * 연관관계 - Foreign Key 값을 따로 컬럼으로 정의하지 않고 연관 관계로 정의합니다.
     */
    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @OneToMany(mappedBy = "columns", orphanRemoval = true)
    List<Card> cards = new LinkedList<>();

    /**
     * 연관관계 편의 메소드 - 반대쪽에는 연관관계 편의 메소드가 없도록 주의합니다.
     */


    /**
     * 서비스 메소드 - 외부에서 엔티티를 수정할 메소드를 정의합니다. (단일 책임을 가지도록 주의합니다.)
     */

    public void updateColumn(ColumnsRequestDto columnsRequestDto) {
        this.title = columnsRequestDto.getTitle();
    }

    public void updateColumnOrder(Integer order) {
        this.columnOrder = Long.valueOf(order);
    }

    public void addCard(Card card) {
        this.cards.add(card);
    }

    public void addNewCard(Card card, Integer order) {
        this.cards.add(order, card);
    }

    public void removeCard(Card card) {
        this.cards.remove(card);
    }

}
