package com.sangbu3jo.elephant.cardcomment.controller;


import com.sangbu3jo.elephant.cardcomment.dto.CardcommentRequestDto;
import com.sangbu3jo.elephant.cardcomment.dto.CardcommentResponseDto;
import com.sangbu3jo.elephant.cardcomment.service.CardcommentService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardcommentController {

    private final CardcommentService cardcommentService;

    /**
     * 카드에 댓글 작성
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 (JWT로 검증)
     * @param card_id: URL에 매핑되어 있는 카드의 ID 값
     * @param cardcommentRequestDto: 댓글 내용을 받아옴
     * @return: 작성한 댓글의 내용(CardcommentResponseDto)과 상태코드 반환
     */
    @PostMapping("/{card_id}/comments")
    public ResponseEntity<CardcommentResponseDto> createCardComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                    @PathVariable Long card_id,
                                                                    @RequestBody CardcommentRequestDto cardcommentRequestDto) {
        CardcommentResponseDto cardcommentResponseDto =
                cardcommentService.createCardComment(userDetails.getUser(), card_id, cardcommentRequestDto);
        return ResponseEntity.ok().body(cardcommentResponseDto);
    }

    /**
     * 카드에 달린 댓글 수정
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 (JWT로 검증)
     * @param comment_id: URL에 매핑되어 있는 카드 댓글의 ID 값
     * @param cardcommentRequestDto: 수정할 댓글의 내용을 받아옴
     * @return: 수정한 댓글의 내용(CardcommentResponseDto)과 상태코드 반환
     */
    @PutMapping("/comments/{comment_id}")
    public ResponseEntity<CardcommentResponseDto> updateCardComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                    @PathVariable Long comment_id,
                                                                    @RequestBody CardcommentRequestDto cardcommentRequestDto) {

        CardcommentResponseDto cardcommentResponseDto =
                cardcommentService.updateCardComment(userDetails.getUser(), comment_id, cardcommentRequestDto);
        return ResponseEntity.ok().body(cardcommentResponseDto);
    }

    /**
     * 카드에 달린 댓글 삭제
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 (JWT로 검증)
     * @param comment_id: URL에 매핑되어 있는 카드 댓글의 ID 값
     * @return: 메세지와 상태코드 반환
     */
    @DeleteMapping("/comments/{comment_id}")
    public ResponseEntity<String> deleteCardComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @PathVariable Long comment_id) {
        try {
            cardcommentService.deleteCardComment(userDetails.getUser(), comment_id);
            return ResponseEntity.ok().body("댓글 삭제 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("댓글이 존재하지 않습니다");
        } catch (AccessDeniedException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

}
