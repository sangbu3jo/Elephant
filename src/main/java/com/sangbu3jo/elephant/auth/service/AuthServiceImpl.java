package com.sangbu3jo.elephant.auth.service;

import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.auth.redis.RefreshToken;
import com.sangbu3jo.elephant.auth.redis.RefreshTokenRepository;
import com.sangbu3jo.elephant.security.jwt.JwtUtil;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@Slf4j(topic = "Auth Service")
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtUtil jwtUtil;

  private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

  @Override
  public String signup(SignupRequestDto signupRequestDto) {
    // 중복 체크
    if (userRepository.existsByUsername(signupRequestDto.getUsername())) {
      log.error("중복된 사용자가 회원가입을 시도하였습니다.");
      throw new IllegalArgumentException("중복된 사용자가 존재합니다.");
    }

    // DB에 저장하기 이전에 데이터 전처리
    String inputUsername = signupRequestDto.getUsername();
    String password = passwordEncoder.encode(signupRequestDto.getPassword());
    String nickname = signupRequestDto.getNickname();
    String introduction = signupRequestDto.getIntroduction();

    // 사용자 ROLE 확인
    UserRoleEnum role = UserRoleEnum.USER;
    if (StringUtils.hasText(signupRequestDto.getAdminToken())) {
      if (!ADMIN_TOKEN.equals(signupRequestDto.getAdminToken())) {
        throw new IllegalArgumentException("관리자 암호가 틀려 등록이 불가능합니다.");
      }
      role = UserRoleEnum.ADMIN;
    }

    // 해당 정보를 생성자 메서드로 User 객체 생성 후 DB 에 저장
    User user = new User(signupRequestDto, password, role);
    userRepository.save(user);

    log.info("회원가입에 성공하였습니다.");
    return "회원가입 성공";
  }

  @Override
  public String generateRefreshToken(HttpServletRequest request, HttpServletResponse response, User user) {
    // 클라이언트 쿠키에서 refresh token 추출
    String clientRefreshToken = jwtUtil.getRefreshTokenFromRequest(request);
    String username = user.getUsername();
    UserRoleEnum role = user.getRole();

    // refresh token 없을 경우 예외 처리
    if (!StringUtils.hasText(clientRefreshToken)) {
      throw new IllegalArgumentException("RefreshToken is null. please login");
    }

    // redis 에서 해당 유저에 따른 refresh token 추출
    RefreshToken refreshToken = refreshTokenRepository.findByUsername(username).get();

    // refresh token 일치 여부 확인
    if (clientRefreshToken.equals(refreshToken.getRefreshToken())) {
      // 엑세스 토큰 생성
      createAccessToken(response, username, role);
    }
    return "Access Token 생성 성공";
  }

  private void createAccessToken(HttpServletResponse response, String username, UserRoleEnum role) {
    // access token 발급 및 쿠키에 저장
    String accessToken = jwtUtil.createToken(username, role);
    jwtUtil.addJwtToCookieAccessToken(accessToken, response);
  }

}
