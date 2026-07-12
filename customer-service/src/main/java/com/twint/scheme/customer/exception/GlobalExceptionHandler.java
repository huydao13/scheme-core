package com.twint.scheme.customer.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(CustomerNotFoundException.class)
  public ProblemDetail handleCustomerNotFound(CustomerNotFoundException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/customer-not-found"));
    pd.setTitle("Customer Not Found");
    return pd;
  }

  @ExceptionHandler(DuplicatePhoneException.class)
  public ProblemDetail handleDuplicatePhone(DuplicatePhoneException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/duplicate-phone"));
    pd.setTitle("Phone Number Already Exists");
    return pd;
  }

  @ExceptionHandler(DeviceLimitExceededException.class)
  public ProblemDetail handleDeviceLimit(DeviceLimitExceededException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/device-limit"));
    pd.setTitle("Device Limit Reached");
    return pd;
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setType(URI.create("https://api.twint.scheme/errors/not-found"));
    pd.setTitle("Not Found");
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
