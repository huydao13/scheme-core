package com.twint.scheme.customer.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String resource, UUID id) {
    super(resource + " " + id + " not found");
  }
}
