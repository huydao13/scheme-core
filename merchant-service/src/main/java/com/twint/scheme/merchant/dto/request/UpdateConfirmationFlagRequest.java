package com.twint.scheme.merchant.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateConfirmationFlagRequest {
  @NotNull
  private Boolean confirmationFlag;
}
