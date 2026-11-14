package com.hms.exception;

public class AuditLogNotFoundException extends ResourceNotFoundException {
  public AuditLogNotFoundException(String message) {
    super(message);
  }
}
