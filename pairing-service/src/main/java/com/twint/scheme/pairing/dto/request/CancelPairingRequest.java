package com.twint.scheme.pairing.dto.request;

import com.twint.scheme.pairing.enumeration.CancelledBy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelPairingRequest {
  @NotBlank
  private String reason;
  @NotNull
  private CancelledBy cancelledBy;
}
