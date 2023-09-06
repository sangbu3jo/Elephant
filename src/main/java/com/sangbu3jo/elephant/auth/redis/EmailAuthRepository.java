package com.sangbu3jo.elephant.auth.redis;


import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
@EnableCaching
@RequiredArgsConstructor
public class EmailAuthRepository {

  private final RedisTemplate redisTemplate;

  // 인증번호 만료시간
  private final long EMAIL_PASSWORD_TIME = 60 * 5; // 5분

  public void save(final EmailAuth emailAuth) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    valueOperations.set(emailAuth.getUsername(),emailAuth.getPassword());
    redisTemplate.expire(emailAuth.getUsername(), EMAIL_PASSWORD_TIME, TimeUnit.SECONDS);
  }

  public Boolean delete(String username) {
    return redisTemplate.delete(username);
  }

  public Optional<EmailAuth> findByUsername(final String username) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    String keyValue = String.valueOf(valueOperations.get(username));

    if (Objects.isNull(keyValue)) {
      return Optional.empty();
    }

    return Optional.of(new EmailAuth(username, keyValue));
  }
}
