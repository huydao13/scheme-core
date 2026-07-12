package com.twint.scheme.merchant.dto.request;

import com.twint.scheme.merchant.enumeration.TerminalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTerminalStatusRequest {

  @NotNull
  private TerminalStatus status;
}
