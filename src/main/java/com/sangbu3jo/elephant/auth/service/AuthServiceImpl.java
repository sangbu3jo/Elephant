package com.sangbu3jo.elephant.auth.service;

import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.auth.redis.RefreshToken;
import com.sangbu3jo.elephant.auth.redis.RefreshTokenRepository;
import com.sangbu3jo.elephant.security.jwt.JwtUtil;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j(topic = "Auth Service")
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtUtil jwtUtil;

  @Value("${signup.admin.key}")
  private String ADMIN_TOKEN;

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
  public Boolean generateAccessToken(HttpServletRequest request, HttpServletResponse response) {
    // 클라이언트 쿠키에서 refresh token 추출
    String InputRefreshToken = jwtUtil.getRefreshTokenFromRequest(request);
    String InputRefreshTokenValue = jwtUtil.substringToken(InputRefreshToken);

    // refresh token 없을 경우 예외 처리
    if (!StringUtils.hasText(InputRefreshToken)) {
      log.error("RefreshToken is null. please login");
      return false;
      //throw new IllegalArgumentException("RefreshToken is null. please login");
    }

    // refresh token 유효성 검사 불일치
    if (!jwtUtil.validateToken(InputRefreshTokenValue)) {
      log.error("Refresh Token does not valid.");
      jwtUtil.deleteCookie(request, response);
      return false;
    }

    // 유저 정보 추출
    Claims claims = jwtUtil.getUserInfoFromToken(InputRefreshTokenValue);
    String username = claims.getSubject();
    UserRoleEnum role = jwtUtil.getUserRole(claims);

    // Redis 의 refresh token 일치 여부 판단
    RefreshToken refreshToken = refreshTokenRepository.findByUsername(username).get();
    if (InputRefreshToken.equals(refreshToken.getRefreshToken())) {
      // 엑세스 토큰 생성
      createAccessToken(response, username, role);
      return true;
    }
    return false;
  }


  @Override
  public String logout(HttpServletRequest request, HttpServletResponse response, User user) {
    // redis refresh token 삭제
    Boolean result = refreshTokenRepository.delete(user.getUsername());
    if (!result) {
      throw new IllegalArgumentException("RefreshToken couldn't deleted.");
    }
    jwtUtil.deleteCookie(request, response);
    return "Logout 성공";
  }

  private void createAccessToken(HttpServletResponse response, String username, UserRoleEnum role) {
    // access token 발급 및 쿠키에 저장
    String accessToken = jwtUtil.createToken(username, role);
    jwtUtil.addJwtToCookieAccessToken(accessToken, response);
  }

}
