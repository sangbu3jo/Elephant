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
//    @GetMapping("/cards/{card_id}")
//    public ResponseEntity<CardOneResponseDto> getOneCard(@PathVariable Long card_id) {
//        CardOneResponseDto cardOneResponseDto = cardService.getOneCard(card_id);
//        return ResponseEntity.ok().body(cardOneResponseDto);
//    }

    @PostMapping("/columns/{column_id}/cards")
    public ResponseEntity<CardResponseDto> createCard(@PathVariable Long column_id,
                                                      @RequestBody CardRequestDto cardRequestDto) {
        CardResponseDto cardResponseDto = cardService.createCard(column_id, cardRequestDto);
        return ResponseEntity.ok().body(cardResponseDto);
    }

    // 카드 순서 정렬
    @PutMapping("/cards/{card_id}/orders")
    public ResponseEntity<String> updateCardOrder(@PathVariable Long card_id,
                                @RequestBody CardOrderRequestDto cardOrderRequestDto) {
        cardService.changeCardOrder(card_id, cardOrderRequestDto);
        return ResponseEntity.ok().body("이동 성공 !");
    }

    @PutMapping("/cards/{card_id}")
    public ResponseEntity<CardResponseDto> updateCard(@PathVariable Long card_id,
                                                      @RequestBody CardRequestDto cardRequestDto) {
        CardResponseDto cardResponseDto = cardService.updateCard(card_id, cardRequestDto);
        return ResponseEntity.ok().body(cardResponseDto);
    }

    @DeleteMapping("/cards/{card_id}")
    public ResponseEntity<String> deleteCard(@PathVariable Long card_id) {
        try {
            cardService.deleteCard(card_id);
            return ResponseEntity.ok().body("카드 삭제 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("카드가 존재하지 않습니다");
        }
    }

    @PatchMapping("/cards/{card_id}")
    public ResponseEntity<String> updateCardUser(@PathVariable Long card_id,
                                                 @RequestBody CardUserRequestDto cardUserRequestDto) {
        cardService.updateCardUser(card_id, cardUserRequestDto);
        return ResponseEntity.ok().body("카드 유저 변경 성공");
    }

    @ResponseBody
    @RequestMapping("/boards/{board_id}/cards")
    public List<CardCalendarResponseDto> getCards(@PathVariable Long board_id) {
        List<CardCalendarResponseDto> cards = cardService.findAllCardsInBoard(board_id);
        return cards;
    }

}
