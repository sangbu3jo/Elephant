package com.sangbu3jo.elephant.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {

  @NotBlank(message = "Id 입력이 안되었습니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String username;

  @NotBlank(message = "비밀번호 입력이 안되었습니다.")
  private String password;

  private String nickname;

  private String introduction;

}
