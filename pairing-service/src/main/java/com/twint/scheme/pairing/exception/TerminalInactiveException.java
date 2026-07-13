package com.twint.scheme.pairing.exception;

import java.util.UUID;

public class TerminalInactiveException extends RuntimeException {
  public TerminalInactiveException(UUID terminalId) {
    super("Terminal " + terminalId + " is not active");
  }
}
