package com.sangbu3jo.elephant.users.controller;


import com.sangbu3jo.elephant.posts.dto.PostCommentResponseDto;
import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.dto.ProfileRequestDto;
import com.sangbu3jo.elephant.users.dto.UpdateUserCommentsDto;
import com.sangbu3jo.elephant.users.dto.UpdateUserPostsDto;
import com.sangbu3jo.elephant.users.dto.UserResponseDto;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;


    // 사용자 정보 조회 API
    @GetMapping("/users/profile")
    public ResponseEntity<UserResponseDto> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserResponseDto userResponseDto = userService.getUserInfo(userDetails.getUser());
        return ResponseEntity.ok(userResponseDto);
    }

    //프로필 수정 API
    @PutMapping("/users/profile")
    public ResponseEntity<String> updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                @RequestBody ProfileRequestDto profileRequestDto){
        String result = userService.updateProfile(userDetails.getUser(), profileRequestDto);
        return ResponseEntity.ok(result);
    }


    // 회원 탈퇴 API
    @DeleteMapping("/users/signOut")
    public ResponseEntity<String> signOut(@AuthenticationPrincipal UserDetailsImpl userDetails){
        String result = userService.signOut(userDetails.getUser());
        return ResponseEntity.ok(result);
    }


    // 작성한 게시물 조회 API
    @GetMapping("/user/posts")
    public ResponseEntity<List<PostResponseDto>> getUserPosts(@AuthenticationPrincipal UserDetailsImpl userDetails){
        List<PostResponseDto> posts = userService.getUserPosts(userDetails.getUser());
        return ResponseEntity.ok(posts);
    }

    // 작성한 게시글 수정
    @PutMapping("/user/posts/{post_id}")
    public ResponseEntity<String> updateUserPosts(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @RequestBody UpdateUserPostsDto updateUserPostsDto,
                                                  @PathVariable Long post_id){
        String result = userService.updateUserPosts(userDetails.getUser(), updateUserPostsDto, post_id);
        return ResponseEntity.ok(result);
    }

    // 작성한 게시글 삭제
    @DeleteMapping("/user/posts/{post_id}")
    public ResponseEntity<String> deleteUserPosts(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @PathVariable Long post_id){
        String result = userService.deleteUserPosts(userDetails.getUser(), post_id);
        return ResponseEntity.ok(result);
    }



    // 작성한 댓글 조회 API
    @GetMapping("/user/comments")
    public ResponseEntity<List<PostCommentResponseDto>> getUserComments(@AuthenticationPrincipal UserDetailsImpl userDetails){
        List<PostCommentResponseDto> comments = userService.getUserComments(userDetails.getUser());
        return ResponseEntity.ok(comments);
    }

    // 작성한 댓글 수정
    @PutMapping("/user/comments/{comment_id}")
    public ResponseEntity<String> updateUserComments(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @RequestBody UpdateUserCommentsDto updateUserCommentsDto,
                                                     @PathVariable Long comment_id){
        String result = userService.updateUserComments(userDetails.getUser(), updateUserCommentsDto, comment_id);
        return ResponseEntity.ok(result);
    }

    // 작성한 댓글 삭제
    @DeleteMapping("/user/comments/{comment_id}")
    public ResponseEntity<String> deleteUserComments(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @PathVariable Long comment_id){
        String result = userService.deleteUserComments(userDetails.getUser(), comment_id);
        return ResponseEntity.ok(result);
    }
}
