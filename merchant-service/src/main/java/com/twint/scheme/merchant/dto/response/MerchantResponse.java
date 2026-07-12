package com.twint.scheme.merchant.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantResponse {
  private UUID id;
  private String name;
  private String mcc;
  private boolean confirmationFlag;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;
  private List<TerminalResponse> terminals;
}
