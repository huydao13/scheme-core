package com.twint.scheme.merchant.exception;

public class MerchantSuspendedException extends RuntimeException {
  public MerchantSuspendedException() {
    super("Merchant is currently suspended and cannot accept payments");
  }
}
