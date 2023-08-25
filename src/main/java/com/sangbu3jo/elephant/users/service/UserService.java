package com.sangbu3jo.elephant.users.service;

import com.sangbu3jo.elephant.posts.dto.PostCommentResponseDto;
import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.entity.PostComment;
import com.sangbu3jo.elephant.posts.repository.PostCommentRepository;
import com.sangbu3jo.elephant.posts.repository.PostRepository;
import com.sangbu3jo.elephant.users.dto.ProfileRequestDto;
import com.sangbu3jo.elephant.users.dto.UpdateUserCommentsDto;
import com.sangbu3jo.elephant.users.dto.UpdateUserPostsDto;
import com.sangbu3jo.elephant.users.dto.UserResponseDto;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postcommentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto getUserInfo(User requestuser) {

        User user = userRepository.findById(requestuser.getId()).orElseThrow(
                () -> new IllegalArgumentException("해당 아이디는 존재하지 않습니다.")
        );

        UserResponseDto userResponseDto = new UserResponseDto(user);
        return userResponseDto;
    }


    // 프로필 수정 메서드
    public String updateProfile(User requestuser, ProfileRequestDto profileRequestDto) {
        // 유저 조회
        User user = userRepository.findById(requestuser.getId()).orElse(null);
        if(user == null){
            return "존재하지 않는 회원입니다.";
        }

        // 프로필수정 요청한 user, 프로필수정 할 user 비교
        if(!requestuser.getId().equals(user.getId())){
            return "본인의 프로필만 수정이 가능합니다.";
        }

        // 프로필 수정
        if(profileRequestDto.getPassword() != null) user.setPassword(passwordEncoder.encode(profileRequestDto.getPassword()));
        if(profileRequestDto.getNickname() != null) user.setNickname(profileRequestDto.getNickname());
        if(profileRequestDto.getIntroduction() != null) user.setIntroduction(profileRequestDto.getIntroduction());

        userRepository.save(user);
        return "프로필 수정이 완료되었습니다.";
    }


    // 회원 탈퇴 메서드
    @Transactional
    public String signOut(User requestuser) {
        // 유저 조회
        User user = userRepository.findById(requestuser.getId()).orElse(null);
        if(user == null){
            return "존재하지 않는 회원입니다.";
        }

        // 회원탈퇴를 요청한 user, 회원탈퇴 할 user 비교
        if(!requestuser.getId().equals(user.getId())){
            return "본인만 회원탈퇴가 가능합니다.";
        }

        // 회원 탈퇴
        postcommentRepository.deleteAllByUser(user);
        postRepository.deleteAllByUser(user);
        userRepository.delete(user);
        return "회원탈퇴가 완료되었습니다.";
    }


    // 작성한 게시물 조회
    public List<PostResponseDto> getUserPosts(User requestuser) {
        // 유저 조회
        User user = userRepository.findById(requestuser.getId()).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 회원입니다.")
        );

        // 게시글 조회를 요청한 user, 게시글 조회를 할 user 비교
        if(!requestuser.getId().equals(user.getId())){
            throw new IllegalArgumentException("본인의 게시글만 조회가 가능합니다.");
        }

        // 게시글 가져오기
        List<Post> posts = postRepository.findByUser(user);
        List<PostResponseDto> postResponseDtos = new ArrayList<>();

        for(Post post : posts){
            postResponseDtos.add(new PostResponseDto(post));
        }

        return postResponseDtos;
    }

    // 작성한 게시글 수정
    @Transactional
    public String updateUserPosts(User requestuser, UpdateUserPostsDto updateUserPostsDto, Long postId) {
        // 게시글 조회
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 게시글입니다.")
        );

        // 작성자 비교
        if(!requestuser.getId().equals(post.getUser().getId())){
            throw new IllegalArgumentException("본인의 게시글만 수정이 가능합니다.");
        }

        if(updateUserPostsDto.getTitle() != null) post.setTitle(updateUserPostsDto.getTitle());
        if(updateUserPostsDto.getContent() != null) post.setContent(updateUserPostsDto.getContent());
        if(updateUserPostsDto.getCompleted() != null) post.setCompleted(updateUserPostsDto.getCompleted());

        postRepository.save(post);
        return "게시글 수정되었습니다";
    }


    // 작성한 게시글 삭제
    public String deleteUserPosts(User requestuser, Long postId) {
        // 게시글 조회
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 게시글입니다.")
        );

        // 작성자 비교
        if(!requestuser.getId().equals(post.getUser().getId())){
            throw new IllegalArgumentException("본인의 게시글만 삭제가 가능합니다.");
        }

        postRepository.delete(post);
        return "게시글이 삭제되었습니다.";
    }


    // 작성한 댓글 조회
    public List<PostCommentResponseDto> getUserComments(User requestuser) {
        // 유저 조회
        User user = userRepository.findById(requestuser.getId()).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 회원입니다.")
        );

        // 댓글 조회를 요청한 user, 댓글 조회를 할 user 비교
        if(!requestuser.getId().equals(user.getId())){
            throw new IllegalArgumentException("본인의 게시글만 조회가 가능합니다.");
        }

        // 댓글 가져오기
        List<PostComment> postcomments = postcommentRepository.findByUser(user);
        List<PostCommentResponseDto> postCommentResponseDtos = new ArrayList<>();

        for(PostComment postComment : postcomments){
            postCommentResponseDtos.add(new PostCommentResponseDto(postComment));
        }

        return postCommentResponseDtos;
    }

    // 작성한 댓글 수정
    @Transactional
    public String updateUserComments(User requestuser, UpdateUserCommentsDto updateUserCommentsDto, Long commentId) {
        // 댓글 조회
        PostComment postComment = postcommentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 댓글입니다.")
        );

        // 작성자 비교
        if(!requestuser.getId().equals(postComment.getUser().getId())){
            throw new IllegalArgumentException("본인의 댓글만 수정이 가능합니다.");
        }

        postComment.setContent(updateUserCommentsDto.getContent());
        return "댓글이 수정되었습니다.";
    }

    // 작성한 댓글 삭제
    public String deleteUserComments(User requestuser, Long commentId) {
        // 댓글 조회
        PostComment postComment = postcommentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 댓글입니다.")
        );

        // 작성자 비교
        if(!requestuser.getId().equals(postComment.getUser().getId())){
            throw new IllegalArgumentException("본인의 댓글만 삭제가 가능합니다.");
        }

        postcommentRepository.delete(postComment);
        return "댓글이 삭제되었습니다.";

    }



}
