package com.twint.scheme.pairing.service;

import com.twint.scheme.pairing.entity.Pairing;
import com.twint.scheme.pairing.enumeration.CancelledBy;
import com.twint.scheme.pairing.enumeration.PairingState;
import com.twint.scheme.pairing.repository.PairingRedisRepository;
import com.twint.scheme.pairing.repository.PairingRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PairingExpirySchedule {
  private final PairingRepository pairingRepository;
  private final PairingRedisRepository redisRepository;

  // Chạy mỗi 10 giây
  @Scheduled(fixedDelay = 10_000)
  @Transactional
  public void expireStaleParings() {
    List<Pairing> expired = pairingRepository
        .findAllByStateAndExpiresAtBefore(PairingState.CREATED, Instant.now());

    for (Pairing pairing : expired) {
      pairing.setState(PairingState.EXPIRED);
      pairing.setCancelledBy(CancelledBy.SYSTEM);
      pairing.setCancelReason("TTL expired");
      redisRepository.blacklistToken(pairing.getToken());
      log.info("Pairing {} expired", pairing.getId());
    }

    if (!expired.isEmpty()) {
      pairingRepository.saveAll(expired);
    }
  }
}
