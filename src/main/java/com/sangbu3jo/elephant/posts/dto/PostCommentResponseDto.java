package com.sangbu3jo.elephant.posts.dto;

import com.sangbu3jo.elephant.posts.entity.PostComment;
import com.sangbu3jo.elephant.posts.service.PostCommentService;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class PostCommentResponseDto {
    private Long id;
    private String username;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public PostCommentResponseDto(PostComment postComment) {
        this.username = postComment.getUser().getUsername();
        this.content = postComment.getContent();
        this.createdAt = postComment.getCreatedAt();
        this.modifiedAt = postComment.getModifiedAt();
        this.id = postComment.getId();
    }
}
