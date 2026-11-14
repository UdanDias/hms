package com.hms.exception;

public class RoomNotFoundException extends ResourceNotFoundException {
  public RoomNotFoundException(String message) {
    super(message);
  }
}
