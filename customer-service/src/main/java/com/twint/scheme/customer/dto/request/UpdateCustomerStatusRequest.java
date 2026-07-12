package com.twint.scheme.customer.dto.request;

import com.twint.scheme.customer.enumeration.CustomerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCustomerStatusRequest {
  @NotNull
  private CustomerStatus status;

  private String reason;
}
