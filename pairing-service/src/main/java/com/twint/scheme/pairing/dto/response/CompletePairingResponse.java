package com.twint.scheme.pairing.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompletePairingResponse {
  private UUID id;
  private String state;
  private UUID orderId;
  private boolean tokenBlacklisted;
  private Instant updatedAt;
}
