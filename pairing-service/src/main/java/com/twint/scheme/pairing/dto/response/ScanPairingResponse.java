package com.twint.scheme.pairing.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScanPairingResponse {
  private UUID pairingId;
  private UUID merchantId;
  private String merchantName;
  private UUID terminalId;
  private String terminalCode;
  private String state;
  private UUID customerId;
}
