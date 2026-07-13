package com.twint.scheme.pairing.exception;

public class TokenExpiredException extends RuntimeException{
  public TokenExpiredException() {
    super("Token has expired, please ask merchant to generate a new QR");
  }
}
