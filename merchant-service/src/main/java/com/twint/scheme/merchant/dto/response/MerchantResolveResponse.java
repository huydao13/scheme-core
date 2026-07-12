package com.twint.scheme.merchant.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantResolveResponse {
  private UUID merchantId;
  private String merchantName;
  private String merchantStatus;
  private boolean confirmationFlag;
  private UUID terminalId;
  private String terminalCode;
  private String terminalType;
  private String terminalStatus;
}
