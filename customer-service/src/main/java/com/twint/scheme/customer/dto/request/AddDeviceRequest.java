package com.twint.scheme.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddDeviceRequest {
  @NotBlank
  private String fingerprint;

  @NotBlank
  private String certificate;
}
