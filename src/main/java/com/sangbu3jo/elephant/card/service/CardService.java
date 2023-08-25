package com.sangbu3jo.elephant.card.service;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.service.BoardService;
import com.sangbu3jo.elephant.boarduser.dto.BoardUserResponseDto;
import com.sangbu3jo.elephant.boarduser.entity.BoardUser;
import com.sangbu3jo.elephant.boarduser.repository.BoardUserRepository;
import com.sangbu3jo.elephant.card.dto.*;
import com.sangbu3jo.elephant.card.entity.Card;
import com.sangbu3jo.elephant.card.repository.CardRepository;
import com.sangbu3jo.elephant.carduser.entity.CardUser;
import com.sangbu3jo.elephant.carduser.repository.CardUserRepository;
import com.sangbu3jo.elephant.columns.entity.Columns;
import com.sangbu3jo.elephant.columns.repository.ColumnsRepository;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "카드 서비스")
@Service
@RequiredArgsConstructor
public class CardService {

    private final BoardService boardService;
    private final CardRepository cardRepository;
    private final ColumnsRepository columnsRepository;
    private final CardUserRepository cardUserRepository;
    private final BoardUserRepository boardUserRepository;
    private final UserRepository userRepository;

    public CardOneResponseDto getOneCard(Long cardId) {
        Card card = findCard(cardId);
        return new CardOneResponseDto(card);
    }

    public CardResponseDto createCard(Long columnId, CardRequestDto cardRequestDto) {
        Columns columns = findColumns(columnId);

        Card card = new Card(cardRequestDto, columns, columns.getCards().size());
        cardRepository.save(card);
        columns.addCard(card);
        return new CardResponseDto(card);
    }

    @Transactional
    public CardResponseDto updateCard(Long cardId, CardRequestDto cardRequestDto) {
        Card card = findCard(cardId);

        card.updateCard(cardRequestDto);

        return new CardResponseDto(card);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        Card card = findCard(cardId);
        Columns columns = card.getColumns();

        columns.removeCard(card);
        cardRepository.delete(card);
    }

    @Transactional
    public void updateCardUser(Long cardId, CardUserRequestDto cardUserRequestDto) {
        // update 하려는 카드의 유저를 찾음
        Card card = findCard(cardId);
        // 우선 card의 cardusers 리스트를 비움
        card.getCardUsers().clear();
        List<CardUser> cardUserList = cardUserRepository.findAllByCard(card); // list 형태로 찾아옴
        cardUserRepository.deleteAll(cardUserList);

        // 카드 유저를 만들어서 저장함
        for (String name : cardUserRequestDto.getCardusernames()) {
            User finduser = userRepository.findByUsername(name).orElseThrow();
            CardUser cardUser = new CardUser(card, finduser);
            cardUserRepository.save(cardUser);
            card.addCardUser(cardUser);
        }
    }

    @Transactional
    public void changeCardOrder(Long cardId, CardOrderRequestDto cardOrderRequestDto) {
        // 이동하는 카드 찾기
        Card card = findCard(cardId);

        // 이동 전, 이동 후의 컬럼 찾기
        Columns oldcolumn = card.getColumns();
        Columns newcolumn = findColumns(cardOrderRequestDto.getColumnId());

        List<Card> oldCardList = oldcolumn.getCards();

        // ID 값이 일치하는 경우라면 (컬럼이 일치한다면)
        if (oldcolumn.getId().equals(newcolumn.getId())) {
            log.info("컬럼 변경 X");

            for (int i = 0; i < oldcolumn.getCards().size(); i++) {
                log.info(oldcolumn.getCards().get(i).getTitle() + ", 순서: " + i);
            }

            // 1. 카드가 해당 컬럼 안에서 순서만 바뀌는 경우
            // => 원래 카드에 저장된 column의 id와 cardOrderRequestDto 에서 가져온 column의 id 값이 일치
            oldcolumn.changeCardOrder(card, cardOrderRequestDto.getCardOrder());

            log.info("이후 순서: " + oldcolumn.getCards().indexOf(card));

            for (int i = 0; i < oldCardList.size(); i++) {
                oldCardList.get(i).updateCardOrder(i);
                cardRepository.save(oldCardList.get(i));
                log.info(oldCardList.get(i).getTitle() + ", 순서: " + i);
            }

            // 예전 컬럼의 내용들 저장
            cardRepository.saveAll(oldCardList);
            columnsRepository.save(oldcolumn);

        } else {
            log.info("컬럼 변경 O");
            // 2. 카드가 다른 컬럼으로 이동하는 경우
            // => 원래 카드에 저장된 column의 id를 받아온 cardOrderRequestDto에서 가져온 column의 id값으로 새로운 column을 찾아서 set 해줌
            card.setColumn(newcolumn);
            oldcolumn.removeCard(card);

            for (int i = 0; i < oldCardList.size(); i++) {
                log.info(oldCardList.get(i).getTitle() + ", index: " + i);
                oldCardList.get(i).updateCardOrder(i);
                cardRepository.save(oldCardList.get(i));
            }

            // 예전 컬럼의 내용들 저장
            cardRepository.saveAll(oldCardList);
            columnsRepository.save(oldcolumn);

            // 새로운 컬럼의 원하는 자리로 이동
            newcolumn.addNewCard(card, cardOrderRequestDto.getCardOrder());
            List<Card> newCardList = cardRepository.findAllByColumnsOrderByCardOrder(newcolumn);

            for (int i = 0; i < newCardList.size(); i++) {
                log.info(newCardList.get(i).getTitle() + ", index: " + i);
                newCardList.get(i).updateCardOrder(i);
                cardRepository.save(newCardList.get(i));
            }

            columnsRepository.save(newcolumn);
            cardRepository.saveAll(newCardList);
        }

    }

    // 카드 유저 , 보드 유저 전체 조회
    public List<BoardUserResponseDto> findCardUsers(Board board, Long cardId) {
        // 해당 카드를 찾음
        Card card = findCard(cardId);
        // 해당 카드에 존재하는 user들을 모두 찾음
        List<CardUser> cardUsers = cardUserRepository.findAllByCard(card);
        // 현재 보드에 존재하는 user들을 모두 찾음
        log.info(board.getId().toString());
        List<BoardUser> boardUsers = boardUserRepository.findAllByBoard(board);
        log.info(String.valueOf(boardUsers.size()));

        List<BoardUserResponseDto> users = new ArrayList<>();

        // 우선 보드 유저에서 해당 사용자들을 users에 추가하도록 함
        for (BoardUser b: boardUsers) {
            if (cardUsers.size() > 0) {
                for (CardUser c: cardUsers) {
                    // 단, cardusers에 해당 사용자가 존재한다면
                    if (b.getUser().getUsername().equals(c.getUser().getUsername())) {
                        // 선택되었다고 표시
                        users.add(new BoardUserResponseDto(b.getUser(), true));
                        log.info(b.getUser().getUsername());
                    } else {
                        // cardusers에 해당 사용자가 없다면 선택되지 않았다고 표시
                        users.add(new BoardUserResponseDto(b.getUser(), false));
                        log.info(b.getUser().getUsername());
                    }
                }
            } else {
                users.add(new BoardUserResponseDto(b.getUser(), false));
                log.info(b.getUser().getUsername());
            }
        }
        return users;
    }

    public List<CardCalendarResponseDto> findAllCardsInBoard(Long boardId) {
        Board board = boardService.findBoard(boardId);
        return cardRepository.findAllByBoard(board).stream().map(CardCalendarResponseDto::new).toList();
    }

    public Columns findColumns(Long columnId) {
        return columnsRepository.findById(columnId).orElseThrow(IllegalArgumentException::new);
    }

    public Card findCard(Long cardId) {
        return cardRepository.findById(cardId).orElseThrow(IllegalArgumentException::new);
    }

    
}
