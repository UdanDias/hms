package com.hms.exception;

public class RoomStatusNotFoundException extends ResourceNotFoundException {
  public RoomStatusNotFoundException(String message) {
    super(message);
  }
}
