package com.twint.scheme.customer.exception;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {
  public CustomerNotFoundException(UUID id) {
    super("Customer " + id + " not found");
  }
  public CustomerNotFoundException(String phone) {
    super("No customer registered with phone " + phone);
  }
}
