package com.twint.scheme.pairing.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(PairingNotFoundException.class)
  public ProblemDetail handlePairingNotFound(PairingNotFoundException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/pairing-not-found"));
    pd.setTitle("Pairing Not Found");
    return pd;
  }

  @ExceptionHandler(TokenNotFoundException.class)
  public ProblemDetail handleTokenNotFound(TokenNotFoundException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/token-not-found"));
    pd.setTitle("Token Not Found");
    return pd;
  }

  @ExceptionHandler(TokenExpiredException.class)
  public ProblemDetail handleTokenExpired(TokenExpiredException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/token-expired"));
    pd.setTitle("Token Expired");
    return pd;
  }

  @ExceptionHandler(TokenAlreadyUsedException.class)
  public ProblemDetail handleTokenUsed(TokenAlreadyUsedException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/token-already-used"));
    pd.setTitle("Token Already Used");
    return pd;
  }

  @ExceptionHandler(InvalidPairingStateException.class)
  public ProblemDetail handleInvalidState(InvalidPairingStateException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/invalid-pairing-state"));
    pd.setTitle("Invalid Pairing State");
    return pd;
  }

  @ExceptionHandler(CannotCancelPairingException.class)
  public ProblemDetail handleCannotCancel(CannotCancelPairingException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/cannot-cancel-pairing"));
    pd.setTitle("Cannot Cancel Pairing");
    return pd;
  }

  @ExceptionHandler(TerminalInactiveException.class)
  public ProblemDetail handleTerminalInactive(TerminalInactiveException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/terminal-inactive"));
    pd.setTitle("Terminal Inactive");
    return pd;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    String detail = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .reduce((a, b) -> a + "; " + b)
        .orElse("Validation failed");
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    pd.setType(URI.create("https://api.twint.scheme/errors/bad-request"));
    pd.setTitle("Bad Request");
    return pd;
  }
}
