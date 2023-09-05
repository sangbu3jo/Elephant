package com.sangbu3jo.elephant.auth.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;

@Builder
@Getter
@AllArgsConstructor
public class EmailAuth {

  // 이 객체는 레디스에 저장되어 이메일 인증 객체 관리하는데 사용된다.
  @Id
  private String username; // email 값 저장

  private String password; // 인즌 번호

}
