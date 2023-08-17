package com.sangbu3jo.elephant.boarduser.repository;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.boarduser.entity.BoardUser;
import com.sangbu3jo.elephant.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface BoardUserRepository extends JpaRepository<BoardUser, Long> {
    List<BoardUser> findAllByUser(User user);

    Optional<BoardUser> findByBoardAndUser(Board board, User user);
}
