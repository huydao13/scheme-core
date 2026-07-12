package com.twint.scheme.customer.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceResponse {
  private UUID id;
  private String fingerprint;
  private String status;
  private boolean isActive;
  private Instant registeredAt;
  private Instant revokedAt;
}
