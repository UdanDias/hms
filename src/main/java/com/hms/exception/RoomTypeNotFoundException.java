package com.hms.exception;

public class RoomTypeNotFoundException extends ResourceNotFoundException {
  public RoomTypeNotFoundException(String message) {
    super(message);
  }
}
