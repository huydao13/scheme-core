package com.twint.scheme.merchant.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MerchantNotFoundException.class)
  public ProblemDetail handleMerchantNotFound(MerchantNotFoundException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/merchant-not-found"));
    pd.setTitle("Merchant Not Found");
    return pd;
  }

  @ExceptionHandler(TerminalNotFoundException.class)
  public ProblemDetail handleTerminalNotFound(TerminalNotFoundException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/terminal-not-found"));
    pd.setTitle("Terminal Not Found");
    return pd;
  }

  @ExceptionHandler(DuplicateTerminalCodeException.class)
  public ProblemDetail handleDuplicateTerminal(DuplicateTerminalCodeException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/duplicate-terminal-code"));
    pd.setTitle("Terminal Code Already Exists");
    return pd;
  }

  @ExceptionHandler(MerchantSuspendedException.class)
  public ProblemDetail handleMerchantSuspended(MerchantSuspendedException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/merchant-suspended"));
    pd.setTitle("Merchant Suspended");
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
