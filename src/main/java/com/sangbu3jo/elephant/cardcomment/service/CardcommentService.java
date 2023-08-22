package com.sangbu3jo.elephant.cardcomment.service;


import com.sangbu3jo.elephant.card.entity.Card;
import com.sangbu3jo.elephant.card.repository.CardRepository;
import com.sangbu3jo.elephant.cardcomment.dto.CardcommentRequestDto;
import com.sangbu3jo.elephant.cardcomment.dto.CardcommentResponseDto;
import com.sangbu3jo.elephant.cardcomment.entity.CardComment;
import com.sangbu3jo.elephant.cardcomment.repository.CardcommentRepository;
import com.sangbu3jo.elephant.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j(topic = "카드 댓글 서비스")
@Service
@RequiredArgsConstructor
public class CardcommentService {

    private final CardcommentRepository cardcommentRepository;
    private final CardRepository cardRepository;

    @Transactional
    public CardcommentResponseDto createCardComment(User user, Long cardId, CardcommentRequestDto cardcommentRequestDto) {
        Card card = cardRepository.findById(cardId).orElseThrow();
        CardComment comment = new CardComment(cardcommentRequestDto, card, user);

        // 해당 카드의 댓글 리스트에 댓글 추가
        card.addCardcomment(comment);
        cardcommentRepository.save(comment);
        return new CardcommentResponseDto(comment);
    }

    @Transactional
    public CardcommentResponseDto updateCardComment(User user, Long commentId, CardcommentRequestDto cardcommentRequestDto) {
        CardComment comment = findCardComment(commentId);

        if (!comment.getUser().equals(user)) {
            throw new AccessDeniedException("작성자만 수정 가능합니다");
        }
        comment.updateComment(cardcommentRequestDto);
        return new CardcommentResponseDto(comment);
    }


    @Transactional
    public void deleteCardComment(User user, Long commentId) {
        CardComment comment = findCardComment(commentId);
        Card card = comment.getCard();

        if (!comment.getUser().equals(user)) {
            throw new AccessDeniedException("작성자만 삭제 가능합니다");
        }
        // 해당 카드의 댓글 리스트에서 댓글 삭제
        card.removeCardcomment(comment);
        cardcommentRepository.delete(comment);
    }


    public CardComment findCardComment(Long commentId) {
        return cardcommentRepository.findById(commentId).orElseThrow(IllegalArgumentException::new);
    }
}
