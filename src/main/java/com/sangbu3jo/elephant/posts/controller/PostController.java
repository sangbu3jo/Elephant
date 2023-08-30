package com.sangbu3jo.elephant.posts.controller;

import com.sangbu3jo.elephant.posts.dto.PostRequestDto;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.repository.PostRepository;
import com.sangbu3jo.elephant.posts.service.PostService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.RejectedExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;

    //게시글 생성
    @PostMapping("/posts")
    public ResponseEntity<String> createPost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @RequestBody PostRequestDto postRequestDto) {


        try {
            postService.createPost(userDetails.getUser(), postRequestDto);
            return ResponseEntity.status(200).body("게시글이 생성되었습니다.");
        } catch (RejectedExecutionException e) {
            return ResponseEntity.status(400).body("생성 권한이 없습니다.");
        } catch (NullPointerException nullPointerException) {
            return ResponseEntity.status(400).body("게시글을 입력해주세요.");
        }
    }


    //게시글 수정
    @PutMapping("/posts/{post_id}")
    public ResponseEntity<String> modifiedPost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @RequestBody PostRequestDto postRequestDto,
                                               @PathVariable Long post_id) {

        Post post = postRepository.findById(post_id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));


        if (userDetails.getUser().getId().equals(post.getUser().getId())) {
            postService.modifiedPost(post, postRequestDto);
            return ResponseEntity.status(200).body("Success");
        } else return ResponseEntity.status(400).body("수정 권한이 없습니다.");
    }


    //게시글 삭제
    @DeleteMapping("/posts/{post_id}")
    public ResponseEntity<String> deletePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @PathVariable Long post_id) {

        Post post = postRepository.findById(post_id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));

        //post fk user_id 값과 로그인한 user id값 비교
        if (userDetails.getUser().getId().equals(post.getUser().getId())) {
            postService.deletePost(post);
            return ResponseEntity.status(200).body("Success");
        } else return ResponseEntity.status(400).body("삭제 권한이 없습니다.");

    }


}


