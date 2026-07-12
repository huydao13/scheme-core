package com.twint.scheme.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddFinancialAccountRequest {
  @NotBlank
  private String iban;

  @NotBlank
  private String bankCode;

  @NotBlank
  private String issuerId;
}
