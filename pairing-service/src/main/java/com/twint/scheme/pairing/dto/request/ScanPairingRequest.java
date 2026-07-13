package com.twint.scheme.pairing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScanPairingRequest {
  @NotBlank
  private String token;
}
