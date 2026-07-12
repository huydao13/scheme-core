package com.twint.scheme.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCustomerRequest {
  @NotBlank
  @Pattern(regexp = "^0[0-9]{9}$", message = "phoneNumber must be 10 digits starting with 0")
  private String phoneNumber;

  @NotBlank
  @Size(max = 100)
  private String alias;
}
