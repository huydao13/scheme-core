package com.twint.scheme.pairing.repository;

import com.twint.scheme.pairing.entity.Pairing;
import com.twint.scheme.pairing.enumeration.PairingState;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PairingRepository extends JpaRepository<Pairing, UUID> {
  Optional<Pairing> findByToken(String token);
  List<Pairing> findAllByStateAndExpiresAtBefore(PairingState state, Instant now);
}
