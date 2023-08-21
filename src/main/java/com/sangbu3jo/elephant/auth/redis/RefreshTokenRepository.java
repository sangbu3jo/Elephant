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
    valueOperations.set(refreshToken.getUserId(),refreshToken.getRefreshToken());
    redisTemplate.expire(refreshToken.getUserId(), RRFRESH_TOKEN_TIME, TimeUnit.SECONDS);
  }

  public Optional<RefreshToken> findById(final String id) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    String refreshToken = String.valueOf(valueOperations.get(id));

    if (Objects.isNull(refreshToken)) {
      return Optional.empty();
    }

    return Optional.of(new RefreshToken(id, refreshToken));
  }
}