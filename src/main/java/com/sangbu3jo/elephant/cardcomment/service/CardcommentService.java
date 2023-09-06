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

    /**
     * 카드에 댓글 작성
     * @param user: 댓글을 작성할 사용자
     * @param cardId: 댓글을 달 카드의 ID
     * @param cardcommentRequestDto: 댓글의 내용을 받아옴
     * @return: 작성한 댓글의 내용(CardcommentResponstDto)를 반환
     */
    @Transactional
    public CardcommentResponseDto createCardComment(User user, Long cardId, CardcommentRequestDto cardcommentRequestDto) {
        Card card = cardRepository.findById(cardId).orElseThrow();
        CardComment comment = new CardComment(cardcommentRequestDto, card, user);

        // 해당 카드의 댓글 리스트에 댓글 추가
        card.addCardcomment(comment);
        cardcommentRepository.save(comment);
        return new CardcommentResponseDto(comment);
    }

    /**
     * 카드에 달린 댓글 수정
     * @param user: 댓글을 단 사용자인지 아닌지 판단하기 위한 사용자의 정보
     * @param commentId: 수정할 댓글의 ID
     * @param cardcommentRequestDto: 수정할 댓글의 내용을 받아옴
     * @return: 수정한 댓글의 내용(CardcommentResponsetDto)를 반환
     */
    @Transactional
    public CardcommentResponseDto updateCardComment(User user, Long commentId, CardcommentRequestDto cardcommentRequestDto) {
        CardComment comment = findCardComment(commentId);

        if (!comment.getUser().equals(user)) {
            throw new AccessDeniedException("작성자만 수정 가능합니다");
        }
        comment.updateComment(cardcommentRequestDto);
        return new CardcommentResponseDto(comment);
    }

    /**
     * 카드에 달린 댓글 삭제
     * @param user: 댓글을 단 사용자인지 아닌지 판단하기 위한 사용자의 정보
     * @param commentId: 삭제할 댓글의 ID
     */
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

    /**
     * 카드에 달린 댓글의 ID로 카드 댓글을 찾음
     * @param commentId: 찾을 카드의 댓글의 ID
     * @return: CardComment를 반환
     */
    public CardComment findCardComment(Long commentId) {
        return cardcommentRepository.findById(commentId).orElseThrow(IllegalArgumentException::new);
    }
}
