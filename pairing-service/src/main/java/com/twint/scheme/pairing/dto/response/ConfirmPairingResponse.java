package com.twint.scheme.pairing.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfirmPairingResponse {
  private UUID id;
  private String state;
  private UUID orderId;
  private Instant updatedAt;
}
