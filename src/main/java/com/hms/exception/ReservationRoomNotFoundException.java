package com.hms.exception;

public class ReservationRoomNotFoundException extends ResourceNotFoundException {
  public ReservationRoomNotFoundException(String message) {
    super(message);
  }
}
