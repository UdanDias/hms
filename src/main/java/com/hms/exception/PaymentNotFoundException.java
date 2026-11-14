package com.hms.exception;

public class PaymentNotFoundException extends ResourceNotFoundException {
  public PaymentNotFoundException(String message) {
    super(message);
  }
}
