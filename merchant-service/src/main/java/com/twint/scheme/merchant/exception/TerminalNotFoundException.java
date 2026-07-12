package com.twint.scheme.merchant.exception;

import java.util.UUID;

public class TerminalNotFoundException extends RuntimeException {
  public TerminalNotFoundException(UUID id) {
    super("Terminal " + id + " not found");
  }
  public TerminalNotFoundException(String terminalCode) {
    super("No active terminal with code " + terminalCode);
  }
}
