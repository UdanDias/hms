package com.hms.exception;

public class ReservationCustomerNotFoundException extends ResourceNotFoundException {
  public ReservationCustomerNotFoundException(String message) {
    super(message);
  }
}
