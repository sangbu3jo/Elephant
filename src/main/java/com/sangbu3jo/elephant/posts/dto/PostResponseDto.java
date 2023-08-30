package com.sangbu3jo.elephant.posts.dto;

import com.sangbu3jo.elephant.posts.entity.Category;
import com.sangbu3jo.elephant.posts.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private Category category;
    private String username;
    private String nickname;
    private String files;
    private Boolean completed;
    private Integer view_cnt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<PostCommentResponseDto> postCommentList;
    private Long userId;

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.username = post.getUser().getUsername();
        this.nickname = post.getUser().getNickname();
        this.content = post.getContent();
        this.category = post.getCategory();
        this.completed = post.getCompleted();
        this.files = post.getFiles();
        this.view_cnt = post.getViewCnt()==null ? 0 : post.getViewCnt();
        this.createdAt = post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
        this.postCommentList = post.getCommentList().stream()
                .map(PostCommentResponseDto::new)
                .sorted(Comparator.comparing(PostCommentResponseDto::getCreatedAt))
                .toList();
        this.userId = post.getUser().getId();


    }


}
