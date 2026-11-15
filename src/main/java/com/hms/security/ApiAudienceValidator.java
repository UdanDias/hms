package com.hms.security;

import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;

public final class ApiAudienceValidator implements OAuth2TokenValidator<Jwt> {
  private final String audience;

  public ApiAudienceValidator(String audience) {
    if (audience == null || audience.isBlank())
      throw new IllegalArgumentException("API audience is required");
    this.audience = audience;
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    return jwt.getAudience() != null
            && jwt.getAudience().contains(audience)
            && jwt.getExpiresAt() != null
            && jwt.getSubject() != null
            && !jwt.getSubject().isBlank()
        ? OAuth2TokenValidatorResult.success()
        : OAuth2TokenValidatorResult.failure(
            new OAuth2Error("invalid_token", "Required claims missing", null));
  }
}
