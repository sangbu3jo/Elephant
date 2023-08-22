package com.sangbu3jo.elephant.board.controller;

import com.sangbu3jo.elephant.board.dto.BoardOneResponseDto;
import com.sangbu3jo.elephant.board.dto.BoardRequestDto;
import com.sangbu3jo.elephant.board.dto.BoardResponseDto;
import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.service.BoardService;
import com.sangbu3jo.elephant.boarduser.dto.BoardUserResponseDto;
import com.sangbu3jo.elephant.columns.dto.ColumnsResponseDto;
import com.sangbu3jo.elephant.columns.service.ColumnsService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j(topic = "보드 컨트롤러")
@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final ColumnsService columnsService;

    // 프로젝트 생성
    @ResponseBody
    @PostMapping("/boards")
    public ResponseEntity<BoardResponseDto> createBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @RequestBody BoardRequestDto boardRequestDto) {
        log.info("보드 생성 시도");
        BoardResponseDto boardResponseDto = boardService.createBoard(userDetails.getUser(), boardRequestDto);
        return ResponseEntity.ok().body(boardResponseDto);
    }

    // 프로젝트 단건 조회 (카드랑 컬럼 완성되면 마무리하기)
    @GetMapping("/boards/{board_id}")
    public String getOneBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                              @PathVariable Long board_id, Model model) {
        log.info("보드 단건 조회 시도");
        BoardOneResponseDto boardOneResponseDto = boardService.getOneBoard(userDetails.getUser(), board_id);
        model.addAttribute("board", boardOneResponseDto);
        List<ColumnsResponseDto> columns = columnsService.findColumnsList(board_id);
        model.addAttribute("columns", columns);
        List<BoardUserResponseDto> boardUsers = boardService.findBoardUsers(board_id);
        model.addAttribute("boardUsers", boardUsers);
        return "board";

    }
//    @ResponseBody
//    @GetMapping("/boards/{board_id}")
//    public ResponseEntity<BoardOneResponseDto> getOneBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
//                                                              @PathVariable Long board_id) {
//        log.info("보드 단건 조회 시도");
//        BoardOneResponseDto boardOneResponseDto = boardService.getOneBoard(userDetails.getUser(), board_id);
//        return ResponseEntity.ok().body(boardOneResponseDto);
//    }

    // 프로젝트 전체 조회
    @GetMapping("/boards")
    public String getBoardList(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                               @RequestParam(required = false, defaultValue = "0", value = "page") Integer pageNo,
                                                               Pageable pageable,
                                                               Model model) {
        log.info("보드 전체 조회 시도");

        pageNo = (pageNo == 0) ? 0 : (pageNo - 1);
        log.info(pageNo.toString());
        Page<BoardResponseDto> boardList = boardService.getBoards(userDetails.getUser(), pageable, pageNo);
        model.addAttribute("boards", boardList);
        return "boards";
    }
    /*@GetMapping("/boards")
    public ResponseEntity<List<BoardResponseDto>> getBoardList(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                               @RequestParam(required = false, defaultValue = "0", value = "page") Integer pageNo,
                                                               Pageable pageable,
                                                               Model model) {
        log.info("보드 전체 조회 시도");
        pageNo = (pageNo == 0) ? 0 : (pageNo - 1);
        List<BoardResponseDto> boardList = boardService.getBoards(userDetails.getUser(), pageable, pageNo);
        model.addAttribute("list", boardList);
        return ResponseEntity.ok().body(boardList);
    }*/

    // 프로젝트 수정
    @ResponseBody
    @PutMapping("/boards/{board_id}")
    public ResponseEntity<BoardResponseDto> updateBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @PathVariable Long board_id,
                                                        @RequestBody BoardRequestDto boardRequestDto) {
        log.info("보드 수정 시도");

        BoardResponseDto boardResponseDto = boardService.updateBoard(board_id, userDetails.getUser(), boardRequestDto);
        return ResponseEntity.ok().body(boardResponseDto);
    }

    // 프로젝트 삭제
    @ResponseBody
    @DeleteMapping("/boards/{board_id}")
    public ResponseEntity<String> deleteBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @PathVariable Long board_id) {
        log.info("보드 삭제 시도");
        try {
            boardService.deleteBoard(board_id, userDetails.getUser());
            return ResponseEntity.ok().body("보드 삭제 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("보드 매너저만 삭제 가능합니다");
        }
    }

    // 프로젝트 유저 초대
    @GetMapping("/boards/{board_id}/member")
    public String inviteUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                             @PathVariable Long board_id, @RequestParam String member) {
        log.info("유저 초대 시도");
        return boardService.inviteUser(userDetails, board_id, member);
    }

    // 프로젝트 떠나기
    @DeleteMapping("/boards/{board_id}/member")
    public ResponseEntity<String> leaveBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                             @PathVariable Long board_id) {
        log.info("유저 초대 시도");
        return boardService.leaveBoard(userDetails.getUser(), board_id);
    }

    // 프로젝트 유저 초대 시 검색
    @GetMapping("/boards/search/{searching}")
    public ResponseEntity<Slice<BoardUserResponseDto>> findUsersToInvite(@PathVariable String searching) {
        Slice<BoardUserResponseDto> users = boardService.search(searching);

        for (BoardUserResponseDto b :  users) {
            log.info(b.getUsername());
            log.info(b.getNickname());
        }

        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/boards/calendar/{board_id}")
    public String showCalendar(@PathVariable Long board_id, Model model) {
        Board board = boardService.findBoard(board_id);
        BoardResponseDto boardResponseDto = new BoardResponseDto(board);
        model.addAttribute("board", boardResponseDto);
        return "cardCalendar";
    }

}
