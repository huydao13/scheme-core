package com.twint.scheme.pairing.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelPairingResponse {
  private UUID id;
  private String state;
  private String cancelledBy;
  private String reason;
  private Instant updatedAt;
}
