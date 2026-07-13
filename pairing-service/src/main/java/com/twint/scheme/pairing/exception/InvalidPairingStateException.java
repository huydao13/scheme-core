package com.twint.scheme.pairing.exception;

import com.twint.scheme.pairing.enumeration.PairingState;

public class InvalidPairingStateException extends RuntimeException{
  public InvalidPairingStateException(PairingState required, PairingState current) {
    super("Pairing must be in " + required + " state to confirm, current state: " + current);
  }
}
