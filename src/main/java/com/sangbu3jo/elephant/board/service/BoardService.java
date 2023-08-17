package com.sangbu3jo.elephant.board.service;

import com.sangbu3jo.elephant.board.dto.BoardRequestDto;
import com.sangbu3jo.elephant.board.dto.BoardResponseDto;
import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
import com.sangbu3jo.elephant.boarduser.entity.BoardUser;
import com.sangbu3jo.elephant.boarduser.entity.BoardUserRoleEnum;
import com.sangbu3jo.elephant.boarduser.repository.BoardUserRepository;
import com.sangbu3jo.elephant.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j(topic = "보드 서비스")
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardUserRepository boardUserRepository;

    // 보드 생성
    @Transactional
    public BoardResponseDto createBoard(User user, BoardRequestDto boardRequestDto) {
        log.info("보드 생성");
        Board board = new Board(boardRequestDto);
        boardRepository.save(board);
        BoardUser boardUser = new BoardUser(board, user, BoardUserRoleEnum.MANAGER);
        boardUserRepository.save(boardUser);

        board.addBoardUser(boardUser);
        return new BoardResponseDto(board);
    }

    // 보드 조회 (사용자 것만)
    public List<BoardResponseDto> getBoards(User user) {
        log.info("보드 전체 조회");
        List<Board> boards = boardRepository.findAllById(boardUserRepository.findAllByUser(user).stream().map(BoardUser::getBoard).map(Board::getId).toList());
        return boards.stream().map(BoardResponseDto::new).toList();
    }

    // 보드 수정 (참여자/매니저)
    @Transactional
    public BoardResponseDto updateBoard(Long boardId, User user, BoardRequestDto boardRequestDto) {
        log.info("보드 수정");
        Board board = findBoard(boardId);
        BoardUser boardUser = findBoardUser(board, user); // 해당 사용자가 보드의 참여자인지 아닌지 확인

        board.updateBoard(boardRequestDto);

        return new BoardResponseDto(board);
    }

    public void deleteBoard(Long boardId, User user) {
        log.info("보드 삭제");
        Board board = findBoard(boardId);

        BoardUser boardUser = findBoardUser(board, user);

        if (! boardUser.getRole().equals(BoardUserRoleEnum.MANAGER) ) {
            throw new IllegalArgumentException();
        }

        /* Board 엔티티 안에 boarduser set 을 orphanremoval = true 속성을 주었기 때문에,
         *  해당 레포지토리에서 따로 찾아서 삭제해줄 필요 없음 */
        boardRepository.delete(board);
    }


    public Board findBoard(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(IllegalArgumentException::new);
    }

    public BoardUser findBoardUser(Board board, User user) {
        return boardUserRepository.findByBoardAndUser(board, user).orElseThrow(IllegalArgumentException::new);
    }

}
