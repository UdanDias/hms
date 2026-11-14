package com.hms.exception;

public class RoomRateNotFoundException extends ResourceNotFoundException {
  public RoomRateNotFoundException(String message) {
    super(message);
  }
}
