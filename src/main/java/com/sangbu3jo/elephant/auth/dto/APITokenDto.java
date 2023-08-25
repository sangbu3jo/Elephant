package com.sangbu3jo.elephant.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class APITokenDto {

  private String accessToken;
  private String refreshToken;

}
