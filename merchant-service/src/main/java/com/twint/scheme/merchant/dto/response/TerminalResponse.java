package com.twint.scheme.merchant.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TerminalResponse {
  private UUID id;
  private UUID merchantId;
  private String terminalCode;
  private String type;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;
}
