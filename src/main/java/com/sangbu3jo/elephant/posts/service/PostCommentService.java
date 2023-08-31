package com.sangbu3jo.elephant.posts.service;

import com.sangbu3jo.elephant.notification.service.NotificationService;
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
    private final NotificationService notificationService;

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

        if (!user.getId().equals(post.getUser().getId())) {
            String notificationContent = user.getNickname() + "님이 회원님의 \"" + post.getTitle() + "\" 게시글에 댓글을 남겼습니다.";
            String notificationUrl = "/posts/" + post.getId(); // 게시글로 이동할 수 있는 URL
            notificationService.createAndSendNotification(post.getUser().getId(), post.getId(), notificationContent, notificationUrl);
        }





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


    public PostComment findById(Long commentId) {
        PostComment postComment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글은 존재하지 않습니다."));

        return postComment;
    }


}
