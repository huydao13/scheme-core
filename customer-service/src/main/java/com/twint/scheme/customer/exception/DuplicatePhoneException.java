package com.twint.scheme.customer.exception;

public class DuplicatePhoneException extends RuntimeException {
  public DuplicatePhoneException(String phone) {
    super("Phone number " + phone + " is already registered");
  }
}
