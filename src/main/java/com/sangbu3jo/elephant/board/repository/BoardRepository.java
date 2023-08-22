package com.sangbu3jo.elephant.board.repository;

import com.sangbu3jo.elephant.board.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Page<Board> findAllByIdIn(List<Long> boardids, Pageable pageable);

}
