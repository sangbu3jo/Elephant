package com.sangbu3jo.elephant.users.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileRequestDto {
    private String password; // 수정할 사용자 비밀번호
    private String nickname; // 수정할 사용자 닉네임
    private String introduction; // 수정할 사용자 자기소개
    private String profileUrl; // 수정할 프로필 이미지
}
