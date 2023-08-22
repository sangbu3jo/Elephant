package com.sangbu3jo.elephant.columns.repository;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.columns.entity.Columns;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColumnsRepository extends JpaRepository<Columns, Long> {
    List<Columns> findAllByBoardOrderByColumnOrder(Board board);
}
