package com.sangbu3jo.elephant.admin.controller;

import com.sangbu3jo.elephant.admin.service.AdminService;
import com.sangbu3jo.elephant.posts.dto.PostCommentRequestDto;
import com.sangbu3jo.elephant.posts.dto.PostCommentResponseDto;
import com.sangbu3jo.elephant.posts.dto.PostRequestDto;
import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AdminController {
    private final AdminService adminService;

    /**
     * 회원 전체 조회 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @return 모든 사용자 정보를 포함하는 ResponseEntity
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@AuthenticationPrincipal UserDetailsImpl userDetails){
        List<User> users = adminService.getAllUsers(userDetails.getUser());
        return ResponseEntity.ok(users);
    }

    /**
     * 게시물 전체 조회 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @return 모든 사용자 게시물 정보를 포함하는 ResponseEntity
     */
    @GetMapping("/posts") // post랑 겹쳐서 url 수정함
    public ResponseEntity<List<PostResponseDto>> getAllUserPosts(@AuthenticationPrincipal UserDetailsImpl userDetails){
        List<PostResponseDto> posts = adminService.getAllUserPosts(userDetails.getUser());
        return ResponseEntity.ok(posts);
    }

    /**
     * 댓글 전체 조회 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @return 모든 사용자 댓글 정보를 포함하는 ResponseEntity
     */
    @GetMapping("/comments")
    public ResponseEntity<List<PostCommentResponseDto>> getAllUserComments(@AuthenticationPrincipal UserDetailsImpl userDetails){
        List<PostCommentResponseDto> comments = adminService.getALlUserComments(userDetails.getUser());
        return ResponseEntity.ok(comments);
    }

    /**
     * 회원 권한 수정 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @param user_id 대상 사용자의 식별자
     * @return 권한 수정 결과를 나타내는 ResponseEntity
     */
    @PutMapping("/{user_id}/auth")
    public ResponseEntity<String> updateAuth(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @PathVariable Long user_id){
        String result = adminService.updateAuth(userDetails.getUser(), user_id);
        return ResponseEntity.ok(result);
    }

    /**
     * 회원 탈퇴 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @param user_id 대상 사용자의 식별자
     * @return 회원 탈퇴 결과를 나타내는 ResponseEntity
     */
    @DeleteMapping("/{user_id}/auth")
    public ResponseEntity<String> deleteAuth(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @PathVariable Long user_id){
        String result = adminService.deleteAuth(userDetails.getUser(), user_id);
        return ResponseEntity.ok(result);
    }

    /**
     * 게시글 숨김처리 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @param postRequestDto 게시글 수정에 사용되는 데이터
     * @param post_id 대상 게시글의 식별자
     * @return 게시글 숨김처리 결과를 나타내는 ResponseEntity
     */
    @PutMapping("/posts/{post_id}/admin")
    public ResponseEntity<String> adminUpdatePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @RequestBody PostRequestDto postRequestDto,
                                                  @PathVariable Long post_id) {

        String result = adminService.adminUpdatePost(userDetails.getUser(),postRequestDto,post_id);
        return ResponseEntity.ok(result);
    }


    /**
     * 게시글 삭제 API
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @param post_id 대상 게시글의 식별자
     * @return 게시글 삭제 결과를 나타내는 ResponseEntity
     */
    @DeleteMapping("/posts/{post_id}/admin")
    public ResponseEntity<String> adminDeletePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @PathVariable Long post_id) {

        String result = adminService.adminDeletePost(userDetails.getUser(), post_id);
        return ResponseEntity.ok(result);
    }

    /**
     * 댓글 수정 API
     *
     * @param comment_id 대상 댓글의 식별자
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @param postCommentRequestDto 댓글 수정에 사용되는 데이터
     * @return 댓글 수정 결과를 나타내는 ResponseEntity
     */
    @PutMapping("/posts/comments/{comment_id}/admin")
    public ResponseEntity<String> adminUpdateComment(@PathVariable Long comment_id,
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @RequestBody PostCommentRequestDto postCommentRequestDto) {

        String result = adminService.adminUpdateComment(comment_id,userDetails.getUser(),postCommentRequestDto);
        return ResponseEntity.ok(result);
    }

    /**
     * 댓글 삭제 API
     *
     * @param comment_id 대상 댓글의 식별자
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @return 댓글 삭제 결과를 나타내는 ResponseEntity
     */
    @DeleteMapping("/posts/comments/{comment_id}/admin")
    public ResponseEntity<String> adminDeleteComment(@PathVariable Long comment_id,
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String result = adminService.adminDeleteComment(comment_id, userDetails.getUser());
        return ResponseEntity.ok(result);
    }

}
