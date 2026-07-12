package com.twint.scheme.customer.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponse {
  private UUID id;
  private String phoneNumber;
  private String alias;
  private String kycStatus;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;
  private List<DeviceResponse> devices;
  private List<FinancialAccountResponse> financialAccounts;
}
