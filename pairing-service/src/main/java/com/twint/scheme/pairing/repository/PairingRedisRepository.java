package com.twint.scheme.pairing.repository;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PairingRedisRepository {
  private final StringRedisTemplate redisTemplate;

  private static final String TOKEN_KEY   = "pairing:token:";
  private static final String USED_KEY    = "pairing:used:";
  private static final Duration TOKEN_TTL = Duration.ofSeconds(30);
  private static final Duration USED_TTL  = Duration.ofHours(1);

  public void saveToken(String token, UUID pairingId) {
    redisTemplate.opsForValue().set(
        TOKEN_KEY + token,
        pairingId.toString(),
        TOKEN_TTL
    );
  }

  public String getPairingIdByToken(String token) {
    return redisTemplate.opsForValue().get(TOKEN_KEY + token);
  }

  public boolean isTokenExpired(String token) {
    return redisTemplate.opsForValue().get(TOKEN_KEY + token) == null;
  }

  public boolean isTokenUsed(String token) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(USED_KEY + token));
  }

  public void blacklistToken(String token) {
    redisTemplate.opsForValue().set(USED_KEY + token, "1", USED_TTL);
    redisTemplate.delete(TOKEN_KEY + token);
  }
}
