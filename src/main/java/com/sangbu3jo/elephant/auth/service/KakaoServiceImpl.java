package com.sangbu3jo.elephant.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangbu3jo.elephant.auth.dto.APIUserInfoDto;
import com.sangbu3jo.elephant.security.jwt.JwtUtil;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j(topic = "KAKAO Login")
@Service
@RequiredArgsConstructor
public class KakaoServiceImpl implements SocialService{

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RestTemplate restTemplate; // 수동 등록한 Bean
  private final JwtUtil jwtUtil;

  private final String CLIENT_ID = "d7da621a3b256dc1ef5cc2ee72d98307";
  private final String KAKAO_REDIRECT_URL = "http://localhost:8080/api/auth/kakao/callback";


  @Override
  public String socialLogin(String code) throws JsonProcessingException {
    // 1. "인가 코드"로 "액세스 토큰" 요청
    String accessToken = getToken(code);

    // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
    APIUserInfoDto apiUserInfoDto = getUserInfo(accessToken);

    // 3. 필요시에 회원가입
    User kakaoUser = registerUserIfNeeded(apiUserInfoDto);

    // 4. JWT 토큰 반환
    String createToken = jwtUtil.createToken(kakaoUser.getUsername());

    return createToken;
  }


  @Override
  public String getToken(String code) throws JsonProcessingException {
    // 요청 URL 만들기
    URI uri = UriComponentsBuilder
        .fromUriString("https://kauth.kakao.com")
        .path("/oauth/token")
        .encode()
        .build()
        .toUri();

    // HTTP Header 생성
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-type","application/x-www-form-urlencoded;charset=utf-8");

    // HTTP Body 생성
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("client_id", CLIENT_ID); // 자신의 REST API 키
    body.add("redirect_uri", KAKAO_REDIRECT_URL);
    body.add("code",code); // 인가 코드

    RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
        .post(uri) // body 가 있으므로 post 메서드
        .headers(headers)
        .body(body);

    // HTTP 요청 보내기
    ResponseEntity<String> response = restTemplate.exchange(
        requestEntity,
        String.class // 반환값 타입은 String
    );

    // HTTP 응답 (JSON) -> 액세스 토큰 값을 반환합니다.
    JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
    return jsonNode.get("access_token").asText();
  }


  @Override
  public APIUserInfoDto getUserInfo(String accessToken) throws JsonProcessingException {
    // 요청 URL 만들기
    URI uri = UriComponentsBuilder
        .fromUriString("https://kapi.kakao.com")
        .path("/v2/user/me")
        .encode()
        .build()
        .toUri();

    // HTTP Header 생성
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + accessToken);
    headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");


    // Http 요청 보내기
    RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
        .post(uri)
        .headers(headers)
        .body(new LinkedMultiValueMap<>());

    // HTTP 요청 보내기
    ResponseEntity<String> response = restTemplate.exchange(
        requestEntity,
        String.class
    );

    JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
    String id = jsonNode.get("id").asText();
    String nickname = jsonNode.get("properties")
        .get("nickname").asText();
    String email = jsonNode.get("kakao_account")
        .get("email").asText();

    return new APIUserInfoDto(id, nickname, email);
  }


  @Override
  public User registerUserIfNeeded(APIUserInfoDto apiUserInfoDto) {
    // DB 에 중복된 Kakao Id 가 있는지 확인
    String kakaoId = apiUserInfoDto.getId();
    User kakaoUser = (User) userRepository.findByKakaoId(kakaoId).orElse(null);

    if (kakaoUser == null) {
      // 카카오 사용자 email 과 동일한 id 를 가진 회원이 있는지 확인
      String kakaoEmail = apiUserInfoDto.getEmail();
      User sameEmailUser = userRepository.findByUsername(kakaoEmail).orElse(null);
      if (sameEmailUser != null) {
        kakaoUser = sameEmailUser;
        // 기존 회원정보에 카카오 Id 추가
        kakaoUser = kakaoUser.kakaoIdUpdate(kakaoId);
      } else {
        // 신규 회원가입
        // password: random UUID
        String password = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(password);

        kakaoUser = new User(apiUserInfoDto, encodedPassword, UserRoleEnum.USER);
        kakaoUser.kakaoIdUpdate(kakaoId);
      }
      userRepository.save(kakaoUser);
    }
    return kakaoUser;
  }

}
