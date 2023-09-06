package com.sangbu3jo.elephant.card.controller;


import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.service.BoardService;
import com.sangbu3jo.elephant.boarduser.dto.BoardUserResponseDto;
import com.sangbu3jo.elephant.card.dto.*;
import com.sangbu3jo.elephant.card.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final BoardService boardService;

    /**
     * 카드 단건 조회
     * @param card_id: URL에 매핑되어 있는 카드의 ID 값
     * @param model: Model에 프론트로 전송할 데이터를 담아서 보냄
     * @return: 반환할 HTML 페이지
     */
    @GetMapping("/cards/{card_id}")
    public String getOneCard(@PathVariable Long card_id, Model model) {
        CardOneResponseDto cardOneResponseDto = cardService.getOneCard(card_id);
        model.addAttribute("card", cardOneResponseDto);
        model.addAttribute("comments", cardOneResponseDto.getCardCommentList());
        // 사용자 추가
        log.info(cardService.findCard(card_id).getColumns().getBoard().getId().toString());
        Board board = boardService.findBoard(cardService.findCard(card_id).getColumns().getBoard().getId());
        List<BoardUserResponseDto> users = cardService.findCardUsers(board, card_id);
        model.addAttribute("boardusers", users);
        return "card";
    }

    /**
     * 카드 생성
     * @param column_id: URL에 매핑되어 있는 컬럼의 ID 값
     * @param cardRequestDto: 카드 제목, 카드 내용, 카드 마감일을 받아옴
     * @return: 생성한 카드의 내용(CardResponseDto)와 상태코드 반환
     */
    @PostMapping("/columns/{column_id}/cards")
    public ResponseEntity<CardResponseDto> createCard(@PathVariable Long column_id,
                                                      @RequestBody CardRequestDto cardRequestDto) {
        CardResponseDto cardResponseDto = cardService.createCard(column_id, cardRequestDto);
        return ResponseEntity.ok().body(cardResponseDto);
    }

    /**
     * 카드 순서 이동
     * @param card_id: URL에 매핑되어 있는 이동할 카드의 ID 값
     * @param cardOrderRequestDto: 이동한 컬럼의 ID 값과 이동한 카드의 위치값을 받아옴
     * @return: 메세지와 상태코드 반환
     */
    @PutMapping("/cards/{card_id}/orders")
    public ResponseEntity<String> updateCardOrder(@PathVariable Long card_id,
                                @RequestBody CardOrderRequestDto cardOrderRequestDto) {
        cardService.changeCardOrder(card_id, cardOrderRequestDto);
        return ResponseEntity.ok().body("이동 성공 !");
    }

    /**
     * 카드 수정
     * @param card_id: URL에 매핑되어 있는 수정할 카드의 ID 값
     * @param cardRequestDto: 수정할 카드 제목, 카드 내용, 카드 마감일을 받아옴
     * @return: 수정한 카드의 내용(CardResponseDto)와 상태코드 반환
     */
    @PutMapping("/cards/{card_id}")
    public ResponseEntity<CardResponseDto> updateCard(@PathVariable Long card_id,
                                                      @RequestBody CardRequestDto cardRequestDto) {
        CardResponseDto cardResponseDto = cardService.updateCard(card_id, cardRequestDto);
        return ResponseEntity.ok().body(cardResponseDto);
    }

    /**
     * 카드 삭제
     * @param card_id: URL에 매핑되어 있는 수정할 카드의 ID 값
     * @return: 메세지와 상태코드 반환
     */
    @DeleteMapping("/cards/{card_id}")
    public ResponseEntity<String> deleteCard(@PathVariable Long card_id) {
        try {
            cardService.deleteCard(card_id);
            return ResponseEntity.ok().body("카드 삭제 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("카드가 존재하지 않습니다");
        }
    }

    /**
     * 카드 담당자 변경
     * @param card_id: URL에 매핑되어 있는 수정할 카드의 ID 값
     * @param cardUserRequestDto: 수정할 카드의 Username 리스트를 받아옴
     * @return: 메세지와 상태코드 반환
     */
    @PatchMapping("/cards/{card_id}")
    public ResponseEntity<String> updateCardUser(@PathVariable Long card_id,
                                                 @RequestBody CardUserRequestDto cardUserRequestDto) {
        cardService.updateCardUser(card_id, cardUserRequestDto);
        return ResponseEntity.ok().body("카드 유저 변경 성공");
    }

    /**
     * 캘린더에 카드 표시
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @return: 해당 보드에 존재하는 카드들의 List
     */
    @ResponseBody
    @RequestMapping("/boards/{board_id}/cards")
    public List<CardCalendarResponseDto> getCards(@PathVariable Long board_id) {
        List<CardCalendarResponseDto> cards = cardService.findAllCardsInBoard(board_id);
        return cards;
    }

}
