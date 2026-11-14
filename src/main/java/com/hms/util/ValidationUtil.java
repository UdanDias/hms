package com.hms.util;

import com.hms.exception.BusinessRuleException;
import jakarta.validation.Validator;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidationUtil {
  private final Validator validator;

  public void require(boolean condition, String message) {
    if (!condition) throw new BusinessRuleException(message);
  }

  public void validate(Object entity) {
    var errors = validator.validate(entity);
    if (!errors.isEmpty())
      throw new IllegalArgumentException(
          errors.stream()
              .map(e -> e.getPropertyPath() + ": " + e.getMessage())
              .sorted()
              .reduce((a, b) -> a + "; " + b)
              .orElse("Invalid request"));
  }

  public void same(Long a, Long b, String what) {
    require(Objects.equals(a, b), what + " must belong to the same hotel/reservation");
  }

  public void text(String value, String what) {
    require(value != null && !value.isBlank(), what + " is required");
  }
}
