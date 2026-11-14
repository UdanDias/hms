package com.hms.exception;

public class HotelNotFoundException extends ResourceNotFoundException {
  public HotelNotFoundException(String message) {
    super(message);
  }
}
