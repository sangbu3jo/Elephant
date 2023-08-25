package com.sangbu3jo.elephant.posts.service;

import com.sangbu3jo.elephant.posts.dto.PostCommentRequestDto;
import com.sangbu3jo.elephant.posts.dto.PostCommentResponseDto;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.entity.PostComment;
import com.sangbu3jo.elephant.posts.repository.PostCommentRepository;
import com.sangbu3jo.elephant.posts.repository.PostRepository;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;

    //댓글 생성
    public void createComment(Long postId, User user, PostCommentRequestDto postCommentRequestDto) {
        //유저 확인
        User loginUser = userRepository.findById(user.getId())
                .orElseThrow(()-> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        //게시물 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("해당 보드가 존재하지 않습니다."));

       PostComment postComment = new PostComment(postCommentRequestDto,user ,post);



        postCommentRepository.save(postComment);



    }


    //댓글 수정
    @Transactional
    public void modifiedComment(PostCommentRequestDto postCommentRequestDto, PostComment postComment) {

       //댓글 수정
        postComment.update(postCommentRequestDto);


    }


    //댓글 삭제
    public void deleteComment(PostComment postComment) {

        postCommentRepository.delete(postComment);
    }

    //댓글 단건 조회
//    public PostCommentResponseDto getComment(Long id, User user) {
//
//        User commentUser = userRepository.findById(user.getId())
//                .orElseThrow(()-> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
//
//        PostComment postComment = postCommentRepository.findById(id)
//                .orElseThrow(()-> new IllegalArgumentException("해당 댓글을 찾아올 수 없습니다."));
//
//        PostCommentResponseDto postCommentResponseDto = new PostCommentResponseDto(postComment);
//
//        return postCommentResponseDto;
//
//
//
//
//    }
}
