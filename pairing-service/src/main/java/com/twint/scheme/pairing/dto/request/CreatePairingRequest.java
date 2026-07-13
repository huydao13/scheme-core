package com.twint.scheme.pairing.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class CreatePairingRequest {
  @NotNull
  private UUID merchantId;
  @NotNull
  private UUID terminalId;
}
