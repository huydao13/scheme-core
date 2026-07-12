package com.twint.scheme.merchant.repository;

import com.twint.scheme.merchant.entity.Terminal;
import com.twint.scheme.merchant.enumeration.TerminalStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminalRepository extends JpaRepository<Terminal, UUID> {
  boolean existsByTerminalCode(String terminalCode);
  Optional<Terminal> findByTerminalCodeAndStatus(String terminalCode, TerminalStatus status);
  Optional<Terminal> findByIdAndMerchantId(UUID id, UUID merchantId);
}
