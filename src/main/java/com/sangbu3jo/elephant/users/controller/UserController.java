package com.sangbu3jo.elephant.users.controller;


import com.sangbu3jo.elephant.posts.dto.PostCommentResponseDto;
import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.dto.ProfileRequestDto;
import com.sangbu3jo.elephant.users.dto.UpdateUserCommentsDto;
import com.sangbu3jo.elephant.users.dto.UpdateUserPostsDto;
import com.sangbu3jo.elephant.users.dto.UserResponseDto;
import com.sangbu3jo.elephant.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;


    /**
     * 사용자 정보 조회 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @return 사용자 정보를 담은 ResponseEntity
     */
    @GetMapping("/users/profile")
    public ResponseEntity<UserResponseDto> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserResponseDto userResponseDto = userService.getUserInfo(userDetails.getUser());
        return ResponseEntity.ok(userResponseDto);
    }

    /**
     * 프로필 수정 API
     *
     * @param userDetails       현재 인증된 사용자의 상세 정보
     * @param profileRequestDto 프로필 수정에 사용되는 데이터
     * @return 프로필 수정 결과를 나타내는 ResponseEntity
     */
    @PutMapping("/users/profile")
    public ResponseEntity<String> updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                @RequestBody ProfileRequestDto profileRequestDto) {
        String result = userService.updateProfile(userDetails.getUser(), profileRequestDto);
        return ResponseEntity.ok(result);
    }

    /**
     * @param userDetails       현재 로그인한 회원 정보
     * @param image             프로필 이미지 수정
     * @return 프로필 수정 결과
     */
    @PostMapping("/users/profile/image")
    public ResponseEntity<String> updateImg(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @RequestParam MultipartFile image

                                           ) throws IOException {

        String result = userService.updateImg(userDetails.getUser(), image);

        return ResponseEntity.ok(result);
    }

    /**
     * 회원 탈퇴 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @return 회원 탈퇴 결과를 나타내는 ResponseEntity
     */
    @DeleteMapping("/users/signOut")
    public ResponseEntity<String> signOut(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String result = userService.signOut(userDetails.getUser());
        return ResponseEntity.ok(result);
    }


    /**
     * 작성한 게시물 조회 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @return 사용자가 작성한 게시물 목록을 담은 ResponseEntity
     */
    @GetMapping("/user/posts")
    public ResponseEntity<List<PostResponseDto>> getUserPosts(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<PostResponseDto> posts = userService.getUserPosts(userDetails.getUser());
        return ResponseEntity.ok(posts);
    }

    /**
     * 작성한 게시글 수정 API
     *
     * @param userDetails        현재 인증된 사용자의 상세 정보
     * @param updateUserPostsDto 게시글 수정에 사용되는 데이터
     * @param post_id            대상 게시글의 식별자
     * @return 게시글 수정 결과를 나타내는 ResponseEntity
     */
    @PutMapping("/user/posts/{post_id}")
    public ResponseEntity<String> updateUserPosts(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @RequestBody UpdateUserPostsDto updateUserPostsDto,
                                                  @PathVariable Long post_id) {
        String result = userService.updateUserPosts(userDetails.getUser(), updateUserPostsDto, post_id);
        return ResponseEntity.ok(result);
    }

    /**
     * 작성한 게시글 삭제 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @param post_id     대상 게시글의 식별자
     * @return 게시글 삭제 결과를 나타내는 ResponseEntity
     */
    @DeleteMapping("/user/posts/{post_id}")
    public ResponseEntity<String> deleteUserPosts(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @PathVariable Long post_id) {
        String result = userService.deleteUserPosts(userDetails.getUser(), post_id);
        return ResponseEntity.ok(result);
    }


    /**
     * 작성한 댓글 조회 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @return 사용자가 작성한 댓글 목록을 담은 ResponseEntity
     */
    @GetMapping("/user/comments")
    public ResponseEntity<List<PostCommentResponseDto>> getUserComments(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<PostCommentResponseDto> comments = userService.getUserComments(userDetails.getUser());
        return ResponseEntity.ok(comments);
    }

    /**
     * 작성한 댓글 수정 API
     *
     * @param userDetails           현재 인증된 사용자의 상세 정보
     * @param updateUserCommentsDto 댓글 수정에 사용되는 데이터
     * @param comment_id            대상 댓글의 식별자
     * @return 댓글 수정 결과를 나타내는 ResponseEntity
     */
    @PutMapping("/user/comments/{comment_id}")
    public ResponseEntity<String> updateUserComments(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @RequestBody UpdateUserCommentsDto updateUserCommentsDto,
                                                     @PathVariable Long comment_id) {
        String result = userService.updateUserComments(userDetails.getUser(), updateUserCommentsDto, comment_id);
        return ResponseEntity.ok(result);
    }

    /**
     * 작성한 댓글 삭제 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @param comment_id  대상 댓글의 식별자
     * @return 댓글 삭제 결과를 나타내는 ResponseEntity
     */
    @DeleteMapping("/user/comments/{comment_id}")
    public ResponseEntity<String> deleteUserComments(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @PathVariable Long comment_id) {
        String result = userService.deleteUserComments(userDetails.getUser(), comment_id);
        return ResponseEntity.ok(result);
    }
}
