package com.sangbu3jo.elephant.email.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailAuthRequestDto {
  @NotBlank
  private String password;
}
