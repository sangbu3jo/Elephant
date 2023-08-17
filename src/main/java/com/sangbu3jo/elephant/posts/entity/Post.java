package com.sangbu3jo.elephant.posts.entity;

import com.sangbu3jo.elephant.common.TimeStamped;
import com.sangbu3jo.elephant.posts.dto.PostRequestDto;
import com.sangbu3jo.elephant.users.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "post")
public class Post extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Category category;


    @Column
    private String files;

    @Column(nullable = false)
    private Boolean completed;

    @Column
    Integer view_cnt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Post(PostRequestDto postRequestDto, User user) {
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
        this.completed = postRequestDto.getCompleted();
        this.user = user;


    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void updatePost(PostRequestDto postRequestDto) {
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
        this.files = postRequestDto.getFiles();
        this.completed = postRequestDto.getCompleted();
    }
}