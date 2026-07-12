package com.twint.scheme.merchant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMerchantRequest {
  @NotBlank
  @Size(max = 200)
  private String name;

  @NotBlank
  @Pattern(regexp = "^[0-9]{4}$", message = "mcc must be a 4-digit numeric string")
  private String mcc;

  private boolean confirmationFlag = false;
}
