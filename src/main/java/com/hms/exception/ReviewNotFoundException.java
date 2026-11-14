package com.hms.exception;

public class ReviewNotFoundException extends ResourceNotFoundException {
  public ReviewNotFoundException(String message) {
    super(message);
  }
}
