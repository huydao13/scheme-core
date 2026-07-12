package com.twint.scheme.customer.exception;

public class DeviceLimitExceededException extends RuntimeException {
  public DeviceLimitExceededException() {
    super("Customer already has 3 active devices");
  }
}
