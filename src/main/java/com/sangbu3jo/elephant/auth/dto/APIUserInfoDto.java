package com.sangbu3jo.elephant.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class APIUserInfoDto {
  private String id;
  private String nickname;
  private String email;

  public APIUserInfoDto(String id, String nickname, String email){
    this.id = id;
    this.nickname = nickname;
    this.email = email;
  }

}
