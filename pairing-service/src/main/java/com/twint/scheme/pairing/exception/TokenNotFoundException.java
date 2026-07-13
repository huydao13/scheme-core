package com.twint.scheme.pairing.exception;

public class TokenNotFoundException extends RuntimeException{
  public TokenNotFoundException(String token) {
    super("Token " + token + " does not exist");
  }
}
