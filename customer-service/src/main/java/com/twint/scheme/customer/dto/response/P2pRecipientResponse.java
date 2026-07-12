package com.twint.scheme.customer.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class P2pRecipientResponse {
  private UUID customerId;
  private String alias;
  private UUID p2pDefaultAccountId;
  private String status;
}
