package com.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityModeValidator {
  public SecurityModeValidator(@Value("${app.security.auth-mode:local-jwt}") String mode) {
    if (!java.util.Set.of("local-jwt", "oauth2").contains(mode))
      throw new IllegalArgumentException("Invalid authentication mode");
  }
}
