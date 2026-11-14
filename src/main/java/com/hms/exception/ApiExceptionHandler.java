package com.hms.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
  public record ApiError(Instant timestamp, int status, String message) {}

  private ResponseEntity<Object> error(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), message));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Object> missing(ResourceNotFoundException e) {
    return error(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(BusinessRuleException.class)
  public ResponseEntity<Object> conflict(BusinessRuleException e) {
    return error(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    ConstraintViolationException.class,
    InvalidRequestException.class,
    java.time.DateTimeException.class
  })
  public ResponseEntity<Object> invalid(Exception e) {
    return error(HttpStatus.BAD_REQUEST, "Invalid input: " + e.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Object> denied(AccessDeniedException e) {
    return error(HttpStatus.FORBIDDEN, "You are not permitted to perform this operation");
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Object> authentication(AuthenticationException e) {
    return error(HttpStatus.UNAUTHORIZED, "Authentication required");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> integrity(DataIntegrityViolationException e) {
    return error(
        HttpStatus.CONFLICT,
        "A unique value already exists, or this record is referenced by another record");
  }

  @ExceptionHandler({CannotAcquireLockException.class, PessimisticLockingFailureException.class})
  public ResponseEntity<Object> locked(Exception e) {
    return error(
        HttpStatus.CONFLICT,
        "The record is being changed by another request; refresh before retrying");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> unexpected(Exception e) {
    log.error("Unexpected service request failure", e);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception e, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    return new ResponseEntity<>(
        new ApiError(Instant.now(), status.value(), "Request could not be processed"),
        headers,
        status);
  }
}
