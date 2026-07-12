package com.twint.scheme.merchant.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MerchantConfirmationResponse {
  private UUID id;
  private boolean confirmationFlag;
  private Instant updatedAt;
}
