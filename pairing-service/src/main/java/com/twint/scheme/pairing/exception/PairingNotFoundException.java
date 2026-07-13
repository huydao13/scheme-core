package com.twint.scheme.pairing.exception;

import java.util.UUID;

public class PairingNotFoundException extends RuntimeException {
  public PairingNotFoundException(UUID id) {
    super("Pairing " + id + " not found");
  }
}
