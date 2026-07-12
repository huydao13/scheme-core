package com.twint.scheme.merchant.exception;

import java.util.UUID;

public class MerchantNotFoundException extends RuntimeException {
  public MerchantNotFoundException(UUID id) {
    super("Merchant " + id + " not found");
  }
}
