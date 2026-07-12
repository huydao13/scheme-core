package com.twint.scheme.merchant.dto.request;

import com.twint.scheme.merchant.enumeration.MerchantStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMerchantStatusRequest {
  @NotNull
  private MerchantStatus status;

  private String reason;
}
