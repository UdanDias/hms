package com.hms.exception;

public class HotelServiceNotFoundException extends ResourceNotFoundException {
  public HotelServiceNotFoundException(String message) {
    super(message);
  }
}
