package com.sangbu3jo.elephant.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangbu3jo.elephant.auth.dto.APITokenDto;
import com.sangbu3jo.elephant.auth.dto.APIUserInfoDto;
import com.sangbu3jo.elephant.auth.redis.RefreshToken;
import com.sangbu3jo.elephant.auth.redis.RefreshTokenRepository;
import com.sangbu3jo.elephant.security.jwt.JwtUtil;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j(topic = "Google Login")
@Service
@RequiredArgsConstructor
public class GoogleServiceImpl implements SocialService{

  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RestTemplate restTemplate;
  private final JwtUtil jwtUtil;


  @Value("${google.client.id}")
  private String CLIENT_ID;

  @Value("${google.client.secret}")
  private String SECRET_KEY;

  @Value("${google.redirect.url}")
  private String REDIRECT_URL;


  @Override
  public APITokenDto socialLogin(String code) throws JsonProcessingException {
    // 1. "인가 코드"로 "액세스 토큰" 요청
    String accessToken = getToken(code);

    // 2. 토큰으로 구글 API 호출 : "액세스 토큰" 으로 "구글 사용자 정보" 가져오기
    APIUserInfoDto apiUserInfoDto = getUserInfo(accessToken);

    // 3. 필요시에 회원가입
    User googleUser = registerUserIfNeeded(apiUserInfoDto);

    // 4. JWT 토큰 생성
    String createToken = jwtUtil.createToken(googleUser.getUsername(),googleUser.getRole());

    // 5. 리프레시 토큰 생성
    RefreshToken refreshToken = jwtUtil.createRefreshToken(googleUser.getUsername(),googleUser.getRole());
    refreshTokenRepository.save(refreshToken); // Redis 에 저장

    return new APITokenDto(createToken, refreshToken.getRefreshToken());
  }


  @Override
  public String getToken(String code) throws JsonProcessingException {
    // 요청 URL 만들기
    URI uri = UriComponentsBuilder
        .fromUriString("https://oauth2.googleapis.com")
        .path("/token")
        .encode()
        .build()
        .toUri();

    // HTTP Header 생성
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-type", "application/x-www-form-urlencoded");
    // headers.set() 코드도 가능

    // HTTP Body 생성
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("client_id", CLIENT_ID);
    body.add("client_secret", SECRET_KEY);
    body.add("redirect_uri",REDIRECT_URL); // 애플리케이션 등록시 설정한 redirect_uri
    body.add("code", code);

    // 방법 1
    ResponseEntity<String> response = restTemplate.postForEntity(uri,new HttpEntity<>(body, headers),String.class);

    // 방법 2
/*    RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
        .post(uri) // body 가 있으므로 post 메서드
        .headers(headers)
        .body(body);

    // HTTP 요청 보내기
    ResponseEntity<String> response = restTemplate.exchange(
        requestEntity,
        String.class // 반환값 타입은 String
    );*/

    // HTTP 응답 (JSON) -> 액세스 토큰 값을 반환합니다.
    JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
    return jsonNode.get("access_token").asText();
  }


  @Override
  public APIUserInfoDto getUserInfo(String accessToken) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<?> entity = new HttpEntity<>(headers);

    ResponseEntity<APIUserInfoDto> response = restTemplate.exchange(
        "https://www.googleapis.com/oauth2/v2/userinfo",
        HttpMethod.GET,
        entity,
        APIUserInfoDto.class
    );

    APIUserInfoDto userInfo = response.getBody();
    return userInfo;
  }

  @Override
  public User registerUserIfNeeded(APIUserInfoDto apiUserInfoDto) {
    // DB 에 중복된 구글 Id 가 있는지 확인
    String googleId = apiUserInfoDto.getId();
    User googleUser = userRepository.findByGoogleId(googleId).orElse(null);

    if (googleUser == null) {
      // 구글 사용자 email 동일한 email 가진 회원이 있는지 확인
      String googleEmail = apiUserInfoDto.getEmail();
      User sameEmailUser = userRepository.findByUsername(googleEmail).orElse(null);
      if (sameEmailUser != null) {
        googleUser = sameEmailUser;
        // 기존 회원정보에 구글 Id 추가
        googleUser = googleUser.googleIdUpdate(googleId);
      } else {
        // 신규 회원가입
        // password: random UUID
        String password = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(password);

        String email = apiUserInfoDto.getEmail();
        apiUserInfoDto.setNickname(email.substring(0,email.indexOf('@')));

        googleUser = new User(apiUserInfoDto, encodedPassword, UserRoleEnum.USER);
        googleUser.googleIdUpdate(googleId);
      }

      userRepository.save(googleUser);
    }
    return googleUser;
  }

}

