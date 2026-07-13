package com.twint.scheme.pairing.exception;

public class TokenAlreadyUsedException extends RuntimeException{
  public TokenAlreadyUsedException() {
    super("This token has already been scanned");
  }
}
