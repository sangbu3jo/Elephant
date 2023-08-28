package com.sangbu3jo.elephant.posts.entity;

import com.sangbu3jo.elephant.common.TimeStamped;
import com.sangbu3jo.elephant.posts.dto.PostRequestDto;
import com.sangbu3jo.elephant.users.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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


    @Column(nullable = false, length = 1000)
    private String content;

//    @Column(nullable = false)
//    @Enumerated(value = EnumType.STRING)
//    private Category category;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Category category;


    @Column
    private String files;




    @Column(nullable = false)
    private Boolean completed;

    @Column
    Integer viewCnt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<PostComment> commentList = new ArrayList<>(); //댓글

    public Post(PostRequestDto postRequestDto, User user) {
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
//        this.files = postRequestDto.getFiles();
        this.completed = postRequestDto.getCompleted();
        this.user = user;


    }
    public Post(Post post, User user) {
        this.title = post.getTitle();
        this.content = post.getContent();
        this.files = post.getFiles();
        this.completed = post.getCompleted();
        this.user = user;
    }


    public void updatePost(PostRequestDto postRequestDto) {
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
//        this.files = postRequestDto.getFiles();
        this.completed = postRequestDto.getCompleted();
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setView_cnt(Integer viewCnt) {
        this.viewCnt = viewCnt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public void setFiles(String files) {
        this.files = files;
    }

}
