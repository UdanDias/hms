package com.hms.exception;

public class AccommodationNotFoundException extends ResourceNotFoundException {
  public AccommodationNotFoundException(String message) {
    super(message);
  }
}
