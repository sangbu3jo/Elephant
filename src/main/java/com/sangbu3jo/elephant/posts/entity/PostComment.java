package com.sangbu3jo.elephant.posts.entity;

import com.sangbu3jo.elephant.common.TimeStamped;
import com.sangbu3jo.elephant.posts.dto.PostCommentRequestDto;
import com.sangbu3jo.elephant.users.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "post_comment")
public class PostComment extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public PostComment(PostCommentRequestDto postCommentRequestDto, User user, Post post) {
        this.content = postCommentRequestDto.getContent();
        this.user = user;
        this.post = post;
    }

    public void update(PostCommentRequestDto postCommentRequestDto) {
        this.content = postCommentRequestDto.getContent();
    }

    public void setContent(String content) {
        this.content = content;
    }
}
