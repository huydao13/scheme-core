package com.twint.scheme.pairing.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PairingResponse {
  private UUID id;
  private String token;
  private String qrCode;
  private UUID merchantId;
  private UUID terminalId;
  private UUID customerId;
  private String state;
  private Instant expiresAt;
  private Instant createdAt;
  private Instant updatedAt;
}
