package com.twint.scheme.merchant.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantStatusResponse {
  private UUID id;
  private String status;
  private Instant updatedAt;
}
