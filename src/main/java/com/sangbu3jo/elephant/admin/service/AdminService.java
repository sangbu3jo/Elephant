package com.sangbu3jo.elephant.admin.service;

import com.sangbu3jo.elephant.posts.dto.PostCommentRequestDto;
import com.sangbu3jo.elephant.posts.dto.PostCommentResponseDto;
import com.sangbu3jo.elephant.posts.dto.PostRequestDto;
import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.entity.PostComment;
import com.sangbu3jo.elephant.posts.repository.PostCommentRepository;
import com.sangbu3jo.elephant.posts.repository.PostRepository;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;


    // 회원 전체 조회
    public List<User> getAllUsers(User requestuser) {
        // 관리자 체크
        if(!requestuser.getRole().equals(UserRoleEnum.ADMIN)){
            throw new IllegalArgumentException("관리자 기능입니다.");
        }

        //
        List<User> users = userRepository.findAll();

        return users;
    }


    // 게시물 전체 조회
    public List<PostResponseDto> getAllUserPosts(User requestuser) {
        // 관리자 체크
        if(!requestuser.getRole().equals(UserRoleEnum.ADMIN)){
            throw new IllegalArgumentException("관리자 기능입니다.");
        }

        // 게시글 가져오기
        List<Post> posts = postRepository.findAll();
        List<PostResponseDto> postResponseDtos = new ArrayList<>();

        for(Post post : posts){
            postResponseDtos.add(new PostResponseDto(post));
        }

        return postResponseDtos;
    }


    // 댓글 전체 조회
    public List<PostCommentResponseDto> getALlUserComments(User requestuser) {
        // 관리자 체크
        if(!requestuser.getRole().equals(UserRoleEnum.ADMIN)){
            throw new IllegalArgumentException("관리자 기능입니다.");
        }

        // 댓글 가져오기
        List<PostComment> postComments = postCommentRepository.findAll();
        List<PostCommentResponseDto> postCommentResponseDtos = new ArrayList<>();

        for(PostComment postComment : postComments){
            postCommentResponseDtos.add(new PostCommentResponseDto(postComment));
        }

        return postCommentResponseDtos;

    }



    // 회원 권한 수정
    public String updateAuth(User requestuser, Long id) {
        // 관리자 체크
        if(!requestuser.getRole().equals(UserRoleEnum.ADMIN)){
            throw new IllegalArgumentException("관리자 기능입니다.");
        }

        // 유저 조회
        User user = userRepository.findById(id).orElse(null);
        if(user == null){
            return "존재하지 않는 회원입니다.";
        }

        // 권한 수정 : 유저면 어드민으로 변경, 어드민이면 유저로 변경
        UserRoleEnum role = user.getRole() == UserRoleEnum.USER ? UserRoleEnum.ADMIN : UserRoleEnum.USER;
        user.setRole(role);
        userRepository.save(user);
        
        return "권한이 수정되었습니다.";
    }

    // 회원 탈퇴
    public String deleteAuth(User requestuser, Long id) {
        // 관리자 체크
        if(!requestuser.getRole().equals(UserRoleEnum.ADMIN)){
            throw new IllegalArgumentException("관리자 기능입니다.");
        }

        // 유저 조회
        User user = userRepository.findById(id).orElse(null);
        if(user == null){
            return "존재하지 않는 회원입니다.";
        }

        // 회원탈퇴
        userRepository.delete(user);

        return "회원이 탈퇴처리 되었습니다.";

    }

    // 게시글 숨김처리
    @Transactional
    public String adminUpdatePost(User requestuser, PostRequestDto postRequestDto, Long postId) {
        // 관리자 체크
        if(!requestuser.getRole().equals(UserRoleEnum.ADMIN)){
            throw new IllegalArgumentException("관리자 기능입니다.");
        }

        // 게시글 조회
        Post post = postRepository.findById(postId).orElse(null);
        if(post == null){
            return "존재하지 않는 게시글입니다.";
        }

        post.setTitle("숨김 처리된 게시글입니다.");
        post.setContent("숨김 처리된 게시글입니다.");
        return "게시글 숨김처리가 완료되었습니다";
    }

    // 게시글 삭제
    public String adminDeletePost(User requestuser, Long postId) {
        // 관리자 체크
        if(!requestuser.getRole().equals(UserRoleEnum.ADMIN)){
            throw new IllegalArgumentException("관리자 기능입니다.");
        }

        // 게시글 조회
        Post post = postRepository.findById(postId).orElse(null);
        if(post == null){
            return "존재하지 않는 게시글입니다.";
        }

        postRepository.delete(post);
        return "게시글 삭제가 완료되었습니다.";

    }

    // 댓글 수정
    @Transactional
    public String adminUpdateComment(Long commentId, User requestuser, PostCommentRequestDto postCommentRequestDto) {
        // 관리자 체크
        if(!requestuser.getRole().equals(UserRoleEnum.ADMIN)){
            throw new IllegalArgumentException("관리자 기능입니다.");
        }

        // 게시글 조회
        PostComment postComment = postCommentRepository.findById(commentId).orElse(null);
        if(postComment == null){
            return "존재하지 않는 게시글입니다.";
        }

        postComment.setContent("숨김 처리된 댓글입니다.");
        return "댓글 숨김처리가 완료되었습니다.";
    }

    // 댓글 삭제
    public String adminDeleteComment(Long commentId, User requestuser) {
        // 관리자 체크
        if(!requestuser.getRole().equals(UserRoleEnum.ADMIN)){
            throw new IllegalArgumentException("관리자 기능입니다.");
        }

        // 게시글 조회
        PostComment postComment = postCommentRepository.findById(commentId).orElse(null);
        if(postComment == null){
            return "존재하지 않는 게시글입니다.";
        }

        postCommentRepository.delete(postComment);
        return "댓글 삭제가 완료되었습니다.";

    }
}
