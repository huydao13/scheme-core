package com.twint.scheme.pairing.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindByTokenResponse {
  private UUID id;
  private String state;
  private UUID merchantId;
  private UUID terminalId;
  private UUID customerId;
  private Instant expiresAt;
}
