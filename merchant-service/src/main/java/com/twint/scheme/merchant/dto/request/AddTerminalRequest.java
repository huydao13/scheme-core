package com.twint.scheme.merchant.dto.request;

import com.twint.scheme.merchant.enumeration.TerminalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddTerminalRequest {
  @NotBlank
  @Size(max = 50)
  private String terminalCode;

  @NotNull
  private TerminalType type;
}
