package com.hms.exception;

public class ServiceChargeNotFoundException extends ResourceNotFoundException {
  public ServiceChargeNotFoundException(String message) {
    super(message);
  }
}
