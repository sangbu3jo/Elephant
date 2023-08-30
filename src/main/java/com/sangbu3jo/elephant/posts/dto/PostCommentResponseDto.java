package com.sangbu3jo.elephant.posts.dto;

import com.sangbu3jo.elephant.posts.entity.PostComment;
import com.sangbu3jo.elephant.posts.service.PostCommentService;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class PostCommentResponseDto {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String content;
    private String postTitle;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long userId;

    public PostCommentResponseDto(PostComment postComment) {
        this.username = postComment.getUser().getUsername();
        this.nickname = postComment.getUser().getNickname();
        this.content = postComment.getContent();
        this.postTitle = postComment.getPost().getTitle();
        this.createdAt = postComment.getCreatedAt();
        this.modifiedAt = postComment.getModifiedAt();
        this.id = postComment.getId();
        this.userId = postComment.getUser().getId();
    }
}
