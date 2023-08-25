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


    @PostMapping("/{card_id}/comments")
    public ResponseEntity<CardcommentResponseDto> createCardComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                    @PathVariable Long card_id,
                                                                    @RequestBody CardcommentRequestDto cardcommentRequestDto) {
        CardcommentResponseDto cardcommentResponseDto =
                cardcommentService.createCardComment(userDetails.getUser(), card_id, cardcommentRequestDto);
        return ResponseEntity.ok().body(cardcommentResponseDto);
    }

    @PutMapping("/comments/{comment_id}")
    public ResponseEntity<CardcommentResponseDto> updateCardComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                    @PathVariable Long comment_id,
                                                                    @RequestBody CardcommentRequestDto cardcommentRequestDto) {

        CardcommentResponseDto cardcommentResponseDto =
                cardcommentService.updateCardComment(userDetails.getUser(), comment_id, cardcommentRequestDto);
        return ResponseEntity.ok().body(cardcommentResponseDto);

    }


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
