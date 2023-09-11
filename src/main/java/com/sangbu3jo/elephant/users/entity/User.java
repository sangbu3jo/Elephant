package com.sangbu3jo.elephant.users.entity;

import com.sangbu3jo.elephant.auth.dto.APIUserInfoDto;
import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
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
@NoArgsConstructor

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

  @Column(name = "nickname")
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

  @Column(name = "profile_url")
  private String profileUrl =  "https://cdn-icons-png.flaticon.com/128/1864/1864497.png";



  @Column(name = "old_profile_url")
  private String oldProfile;




  @Builder
  public User(SignupRequestDto signupRequestDto, String password, UserRoleEnum role) {
    this.username = signupRequestDto.getUsername();
    this.nickname = signupRequestDto.getNickname();
    this.password = password;
    this.introduction = signupRequestDto.getIntroduction();
    this.role = role;
  }


  @Builder
  public User(APIUserInfoDto apiUserInfoDto, String password, UserRoleEnum role) {
    this.username = apiUserInfoDto.getEmail();
    this.nickname = apiUserInfoDto.getNickname();
    this.password = password;
    this.role = role;
  }

  public User kakaoIdUpdate(String kakaoId){
    this.kakaoId = kakaoId;
    return this;
  }

  public User googleIdUpdate(String googleId){
    this.googleId = googleId;
    return this;
  }

  public User naverIdUpdate(String naverId){
    this.naverId = naverId;
    return this;
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

  public void setId(Long id) {
    this.id = id;
  }


  public void setProfileUrl(String profileUrl) {
    this.profileUrl = profileUrl;
  }



  public void setOldProfile(String oldProfile) {
    this.oldProfile = oldProfile;
  }

}
