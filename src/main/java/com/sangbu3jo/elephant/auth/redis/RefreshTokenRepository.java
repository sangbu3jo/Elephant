package com.sangbu3jo.elephant.auth.redis;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

  private final RedisTemplate redisTemplate;

  // 리프레스 토큰 만료시간
  private final long RRFRESH_TOKEN_TIME = 60 * 60 * 1000L; // 60분

  public void save(final RefreshToken refreshToken) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    valueOperations.set(refreshToken.getUsername(),refreshToken.getRefreshToken());
    redisTemplate.expire(refreshToken.getUsername(), RRFRESH_TOKEN_TIME, TimeUnit.SECONDS);
  }

  public Boolean delete(String username) {
    return redisTemplate.delete(username);
  }

/*
  public Boolean findByAndDelete(String username){
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    String keyValue = String.valueOf(valueOperations.get(username));

    if (Objects.isNull(keyValue)) {
      return false;
    }
    return this.delete(keyValue);
  }
*/

  public Optional<RefreshToken> findByUsername(final String username) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    String keyValue = String.valueOf(valueOperations.get(username));

    if (Objects.isNull(keyValue)) {
      return Optional.empty();
    }

    return Optional.of(new RefreshToken(username, keyValue));
  }
}