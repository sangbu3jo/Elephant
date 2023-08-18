package com.sangbu3jo.elephant.posts.controller;

import com.sangbu3jo.elephant.posts.dto.PostCommentRequestDto;
import com.sangbu3jo.elephant.posts.entity.PostComment;
import com.sangbu3jo.elephant.posts.repository.PostCommentRepository;
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
    private final PostCommentRepository postCommentRepository;

    //댓글 생성

    @PostMapping("/posts/{post_id}/comments")
    public ResponseEntity<String> createComment(@PathVariable Long post_id,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                @RequestBody PostCommentRequestDto postCommentRequestDto) {

        try {
            postCommentService.createComment(post_id, userDetails.getUser(), postCommentRequestDto);
            return ResponseEntity.status(200).body("댓글 생성 완료");
        } catch (RejectedExecutionException e) {
            return ResponseEntity.status(400).body("댓글 생성 실패");
        }
    }

    //댓글 수정
    @PutMapping("/posts/comments/{comment_id}")
    public ResponseEntity<String> modifiedComment(@PathVariable Long comment_id,
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @RequestBody PostCommentRequestDto postCommentRequestDto) {

        PostComment postComment = postCommentRepository.findById(comment_id)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글은 존재하지 않습니다."));

        if (userDetails.getUser().getId().equals(postComment.getUser().getId())) {
            postCommentService.modifiedComment(postCommentRequestDto, postComment);
            return ResponseEntity.ok().body("수정에 성공했습니다.");
        } else return ResponseEntity.badRequest().body("수정 권한이 없습니다.");
    }

    //댓글 삭제
    @DeleteMapping("/posts/comments/{comment_id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long comment_id,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PostComment postComment = postCommentRepository.findById(comment_id)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글은 존재하지 않습니다."));

        if (userDetails.getUser().getId().equals(postComment.getUser().getId())) {
            postCommentService.deleteComment(postComment);
            return ResponseEntity.ok().body("삭제에 성공했습니다.");
        } else return ResponseEntity.badRequest().body("삭제 권한이 없습니다.");
    }

}

