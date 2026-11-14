package com.hms.exception;

public class RefundNotFoundException extends ResourceNotFoundException {
  public RefundNotFoundException(String message) {
    super(message);
  }
}
