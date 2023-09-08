package com.sangbu3jo.elephant.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.auth.redis.RefreshToken;
import com.sangbu3jo.elephant.auth.redis.RefreshTokenRepository;
import com.sangbu3jo.elephant.security.jwt.JwtUtil;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)

class AuthServiceImplTest {

  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private UserRepository userRepository;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  private AuthServiceImpl authService;

  @BeforeEach
  void setUp() {
    // Mokito 초기화
    MockitoAnnotations.openMocks(this);

    // AutgServiceImpl 초기화
    authService = new AuthServiceImpl(passwordEncoder, userRepository, refreshTokenRepository, jwtUtil);
  }


  @Test
  @DisplayName("회원가입")
  void singup() {
    // given
    SignupRequestDto signupRequestDto = new SignupRequestDto();
    signupRequestDto.setUsername("user1@gmail.com");
    signupRequestDto.setPassword("**aa1234");
    signupRequestDto.setNickname("user1");
    signupRequestDto.setIntroduction("this is a just test");

    String password = passwordEncoder.encode(signupRequestDto.getPassword());
    User user = new User(signupRequestDto, password, UserRoleEnum.USER);

    // when
    when(userRepository.existsByUsername(any(String.class))).thenReturn(false);
    when(passwordEncoder.encode(any(String.class))).thenReturn(password);
    when(userRepository.save(any(User.class))).thenReturn(user);

    // when
    String result = authService.signup(signupRequestDto);

    // then
    verify(userRepository).existsByUsername("user1@gmail.com");
    assertEquals("회원가입 성공", result);
    verify(userRepository).save(any(User.class));
  }

  @Disabled
  @Test
  @DisplayName("토큰 재발급")
  void generateAccessToken(){
   /* // Given
    String inputRefreshToken = "your_input_refresh_token";
    String inputRefreshTokenValue = "your_input_refresh_token_value";
    Claims claims = mock(Claims.class);
    UserRoleEnum role = UserRoleEnum.USER;

    // Mocking jwtUtil methods
    when(jwtUtil.getRefreshTokenFromRequest(request)).thenReturn(inputRefreshToken);
    when(jwtUtil.substringToken(inputRefreshToken)).thenReturn(inputRefreshTokenValue);
    when(jwtUtil.validateToken(inputRefreshTokenValue)).thenReturn(true);
    when(jwtUtil.getUserInfoFromToken(inputRefreshTokenValue)).thenReturn(claims);
    when(claims.getSubject()).thenReturn("username");
    when(jwtUtil.getUserRole(claims)).thenReturn(role);

    // Mocking refreshTokenRepository
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setRefreshToken(inputRefreshToken);
    when(refreshTokenRepository.findByUsername("username")).thenReturn(java.util.Optional.of(refreshToken));

    // When
    boolean result = authService.generateAccessToken(request, response);

    // Then
    assertTrue(result);*/
  }


}