package com.twint.scheme.merchant.exception;

public class DuplicateTerminalCodeException extends RuntimeException {
  public DuplicateTerminalCodeException(String terminalCode) {
    super("Terminal code " + terminalCode + " is already registered in the system");
  }
}
