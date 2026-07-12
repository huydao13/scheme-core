package com.twint.scheme.customer.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinancialAccountResponse {
  private UUID id;
  private String iban;
  private String bankCode;
  private String issuerId;
  private boolean isP2pDefault;
  private String status;
  private Instant createdAt;
}
