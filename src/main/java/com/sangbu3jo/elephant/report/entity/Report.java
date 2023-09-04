package com.sangbu3jo.elephant.report.entity;

import com.sangbu3jo.elephant.common.TimeStamped;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.users.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Report extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 신고사유
    @Column(nullable = false)
    private String reason;

    public Report(Post post, User user, String reason){
        this.post = post;
        this.user = user;
        this.reason = reason;
    }
}
