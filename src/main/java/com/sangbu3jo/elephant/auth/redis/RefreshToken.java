package com.sangbu3jo.elephant.auth.redis;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;

@Builder
@Getter
@AllArgsConstructor
public class RefreshToken {
  // 이 객체는 레디스에 저장되어 리프레시 토큰을 관리하는데 사용된다.
  @Id
  private String username; // email id 값 저장

  private String refreshToken;

}
