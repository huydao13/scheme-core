package com.twint.scheme.customer.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class P2pCheckRequest {
  @NotNull
  @Min(1)
  private Long amount;

  @NotBlank
  private String currency;


}
