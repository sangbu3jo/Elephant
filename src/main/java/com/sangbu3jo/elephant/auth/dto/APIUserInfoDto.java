package com.sangbu3jo.elephant.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class APIUserInfoDto {

  private String id;
  private String nickname;
  private String email;

}
