package com.sangbu3jo.elephant.users.entity;


import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.entity.PostComment;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// lombok
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// jpa
@Entity

@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false, updatable = false)
  private Long id;

  @Column(name = "username", nullable = false, unique = true, length = 25)
  private String username;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "nickname", nullable = false, unique = true,  length = 25)
  private String nickname;

  @Column(name = "introduction")
  private String introduction;

  @Column(name = "kakao_id")
  private String kakaoId;

  @Column(name = "naver_id")
  private String naverId;

  @Column(name = "google_id")
  private String googleId;

  @Column(nullable = false)
  @Enumerated(value = EnumType.STRING)
  private UserRoleEnum role;

//  @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
//  private List<Post> posts = new ArrayList<>();

  @Builder
  public User(String username, String password, String nickname, String intruduction, UserRoleEnum role) {
    this.username = username;
    this.password = password;
    this.nickname = nickname;
    this.introduction = intruduction;
    this.role = role;
  }

  public User(String username, String password, String nickname, String googleId) {
    this.username = username;
    this.password = password;
    this.nickname = nickname;
    this.googleId = googleId;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public void setIntroduction(String introduction) {
    this.introduction = introduction;
  }

  public void setRole(UserRoleEnum role) {
    this.role = role;
  }
}
