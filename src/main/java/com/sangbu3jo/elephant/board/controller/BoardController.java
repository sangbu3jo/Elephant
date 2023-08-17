package com.sangbu3jo.elephant.board.controller;

import com.sangbu3jo.elephant.board.dto.BoardRequestDto;
import com.sangbu3jo.elephant.board.dto.BoardResponseDto;
import com.sangbu3jo.elephant.board.service.BoardService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j(topic = "보드 컨트롤러")
@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 보드 생성
    @ResponseBody
    @PostMapping("/boards")
    public ResponseEntity<BoardResponseDto> createBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @RequestBody BoardRequestDto boardRequestDto) {
        log.info("보드 생성 시도");
        BoardResponseDto boardResponseDto = boardService.createBoard(userDetails.getUser(), boardRequestDto);
        return ResponseEntity.ok().body(boardResponseDto);
    }

    // 보드 단건 조회 (카드랑 컬럼 완성되면 마무리하기)

    // 보드 전체 조회
    @GetMapping("/boards")
    @ResponseBody
    public ResponseEntity<List<BoardResponseDto>> getBoardList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("보드 전체 조회 시도");
        List<BoardResponseDto> boardList = boardService.getBoards(userDetails.getUser());
        return ResponseEntity.ok().body(boardList);
    }

    // 보드 수정
    @ResponseBody
    @PutMapping("/boards/{board_id}")
    public ResponseEntity<BoardResponseDto> updateBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @PathVariable Long board_id,
                                                        @RequestBody BoardRequestDto boardRequestDto) {
        log.info("보드 수정 시도");

        BoardResponseDto boardResponseDto = boardService.updateBoard(board_id, userDetails.getUser(), boardRequestDto);
        return ResponseEntity.ok().body(boardResponseDto);
    }

    // 보드 삭제
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


}
