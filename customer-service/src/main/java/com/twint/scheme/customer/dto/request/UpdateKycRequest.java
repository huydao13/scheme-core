package com.twint.scheme.customer.dto.request;

import com.twint.scheme.customer.enumeration.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateKycRequest {
  @NotNull
  private KycStatus kycStatus;
}
