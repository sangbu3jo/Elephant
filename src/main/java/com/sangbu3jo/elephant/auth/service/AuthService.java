package com.sangbu3jo.elephant.auth.service;

import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public String signup(SignupRequestDto signupRequestDto) {
    String inputUsername = signupRequestDto.getUsername();
    String password = passwordEncoder.encode(signupRequestDto.getPassword());
    String nickname = signupRequestDto.getNickname();
    String introduction = signupRequestDto.getIntroduction();

    User user = new User(inputUsername, password, nickname, introduction, UserRoleEnum.USER);

    userRepository.save(user);
    return "회원가입 성공";
  }
}
