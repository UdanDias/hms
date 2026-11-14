package com.hms.exception;

public class InvoiceItemNotFoundException extends ResourceNotFoundException {
  public InvoiceItemNotFoundException(String message) {
    super(message);
  }
}
