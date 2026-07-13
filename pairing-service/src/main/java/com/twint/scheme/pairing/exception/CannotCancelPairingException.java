package com.twint.scheme.pairing.exception;

import com.twint.scheme.pairing.enumeration.PairingState;

public class CannotCancelPairingException extends RuntimeException {
  public CannotCancelPairingException(PairingState state) {
    super("Pairing in " + state + " state cannot be cancelled");
  }
}
