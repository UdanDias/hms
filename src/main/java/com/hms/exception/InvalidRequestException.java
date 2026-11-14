package com.hms.exception;

/** Invalid caller input, kept distinct from JPA API misuse exceptions. */
public class InvalidRequestException extends RuntimeException {
  public InvalidRequestException(String message) {
    super(message);
  }
}
