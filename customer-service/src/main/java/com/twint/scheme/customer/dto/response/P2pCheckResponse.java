package com.twint.scheme.customer.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class P2pCheckResponse {
  private boolean eligible;
  private UUID p2pDefaultAccountId;
  private Long dailyLimitRemaining;
}
