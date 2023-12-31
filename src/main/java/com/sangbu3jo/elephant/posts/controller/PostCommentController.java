package com.sangbu3jo.elephant.posts.controller;

import com.sangbu3jo.elephant.posts.dto.PostCommentRequestDto;
import com.sangbu3jo.elephant.posts.entity.PostComment;
import com.sangbu3jo.elephant.posts.service.PostCommentService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.RejectedExecutionException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;


    /**
     * 댓글 생성
     * @param post_id 게시글 id
     * @param userDetails 로그인한 회원
     * @param postCommentRequestDto  댓글 정보
     * @return 성공 실패 여부 ResponseEntity로 반환
     */

    @PostMapping("/posts/{post_id}/comments")
    public ResponseEntity<String> createComment(@PathVariable Long post_id,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                @RequestBody PostCommentRequestDto postCommentRequestDto) {

        try {
            postCommentService.createComment(post_id, userDetails.getUser(), postCommentRequestDto);
            return ResponseEntity.status(200).body("댓글 생성 완료");
        } catch (RejectedExecutionException e) {
            return ResponseEntity.status(400).body("댓글 생성 실패");
        } catch (NullPointerException nullPointerException) {
            return ResponseEntity.status(400).body("댓글을 입력해주세요");
        }
    }

    /**
     * 댓글 수정
     * @param comment_id 수정할 댓글 id
     * @param userDetails 로그인한 회원 정보
     * @param postCommentRequestDto 새 댓글
     * @return 성공 실패 여부 ResponseEntity로 반환
     */
    @PutMapping("/posts/comments/{comment_id}")
    public ResponseEntity<String> modifiedComment(@PathVariable Long comment_id,
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @RequestBody PostCommentRequestDto postCommentRequestDto) {

        PostComment postComment = postCommentService.findById(comment_id);

        if (userDetails.getUser().getId().equals(postComment.getUser().getId())) {
            postCommentService.modifiedComment(postCommentRequestDto, postComment);
            return ResponseEntity.ok().body("수정에 성공했습니다.");
        } else return ResponseEntity.badRequest().body("수정 권한이 없습니다.");
    }

    /**
     * 댓글 삭제
     * @param comment_id 삭제할 댓글 id
     * @param userDetails 로그인한 회원 정보
     * @return 성공 실패 여부 ResponseEntity로 반환
     */
    @DeleteMapping("/posts/comments/{comment_id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long comment_id,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {

        PostComment postComment = postCommentService.findById(comment_id);

        if (userDetails.getUser().getId().equals(postComment.getUser().getId())) {
            postCommentService.deleteComment(postComment);
            return ResponseEntity.ok().body("삭제에 성공했습니다.");
        } else return ResponseEntity.badRequest().body("삭제 권한이 없습니다.");
    }

}

