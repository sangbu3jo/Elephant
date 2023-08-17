package com.sangbu3jo.elephant.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequestDto {
  @NotBlank
  private String username;

  @NotBlank
  private String password;
}
