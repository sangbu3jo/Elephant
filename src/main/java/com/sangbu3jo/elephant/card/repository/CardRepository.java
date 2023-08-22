package com.sangbu3jo.elephant.card.repository;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.card.entity.Card;
import com.sangbu3jo.elephant.columns.entity.Columns;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findAllByColumnsOrderByCardOrder(Columns columns);

    List<Card> findAllByBoard(Board board);
}
