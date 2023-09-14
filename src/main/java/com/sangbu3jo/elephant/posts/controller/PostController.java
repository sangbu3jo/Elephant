package com.sangbu3jo.elephant.posts.controller;

import com.sangbu3jo.elephant.posts.dto.PostRequestDto;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.service.PostService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import jakarta.validation.Valid;
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


    /**
     *
     * @param userDetails 로그인한 유저 정보
     * @param postRequestDto 클라이언트에서 받아온 값
     * @return 성공 실패 ResponseEntity 반환
     */
    //게시글 생성
    @PostMapping("/posts")
    public ResponseEntity<String> createPost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @Valid @RequestBody PostRequestDto postRequestDto) {


        try {
            postService.createPost(userDetails.getUser(), postRequestDto);
            return ResponseEntity.status(200).body("게시글이 생성되었습니다.");
        } catch (RejectedExecutionException e) {
            return ResponseEntity.status(400).body("생성 권한이 없습니다.");
        } catch (NullPointerException nullPointerException) {
            return ResponseEntity.status(400).body("게시글을 입력해주세요.");
        }
    }


    /**
     *
     * @param userDetails 로그인한 유저 정보
     * @param postRequestDto 클라이언트에서 받아온 값
     * @param postId 수정할 게시글 id
     * @return 성공 실패 ResponseEntity 반환
     * @throws Exception 예외처리
     */
   // 게시글 수정
    @PutMapping("/posts/{post_id}")
    public ResponseEntity<String> modifiedPost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @Valid @RequestBody PostRequestDto postRequestDto,
                                               @PathVariable(value = "post_id") Long postId
                                               ) throws Exception {

        Post post = postService.findById(postId);


        if (userDetails.getUser().getId().equals(post.getUser().getId())) {
            postService.modifiedPost(post, postRequestDto, postId);
            return ResponseEntity.status(200).body("Success");
        } else return ResponseEntity.status(400).body("수정 권한이 없습니다.");
    }


    /**
     *
     * @param userDetails 로그인한 유저 정보
     * @param post_id 삭제할 게시글 id
     * @return 성공 실패 ResponseEntity 반환
     */
    //게시글 삭제
    @DeleteMapping("/posts/{post_id}")
    public ResponseEntity<String> deletePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @PathVariable Long post_id) {

        Post post = postService.findById(post_id);

        //post fk user_id 값과 로그인한 user id값 비교
        if (userDetails.getUser().getId().equals(post.getUser().getId())) {
            postService.deletePost(post);
            return ResponseEntity.status(200).body("Success");
        } else return ResponseEntity.status(400).body("삭제 권한이 없습니다.");

    }


}


