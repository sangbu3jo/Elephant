package com.sangbu3jo.elephant.posts.dto;

import com.sangbu3jo.elephant.posts.entity.Category;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.entity.PostComment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter

public class PostResponseDto {

    private String title;
    private String content;
    private Category category;
    private String files;
    private Boolean completed;
    private Integer view_cnt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<PostCommentResponseDto> postCommentList;

    public PostResponseDto(Post post) {
        this.title = post.getTitle();
        this.content = post.getContent();
        this.category = post.getCategory();
        this.completed = post.getCompleted();
        this.files = post.getFiles();
        this.view_cnt = post.getViewCnt();
        this.createdAt = post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
        this.postCommentList = post.getCommentList().stream()
                .map(PostCommentResponseDto::new)
                .sorted(Comparator.comparing(PostCommentResponseDto::getCreatedAt))
                .toList();


    }

}
