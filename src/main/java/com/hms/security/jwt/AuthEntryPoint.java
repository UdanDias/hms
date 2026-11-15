package com.hms.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper mapper;

  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException error)
      throws IOException {
    response.setStatus(401);
    response.setContentType("application/json");
    response.setHeader("WWW-Authenticate", "Bearer");
    mapper.writeValue(
        response.getOutputStream(),
        Map.of(
            "timestamp",
            Instant.now().toString(),
            "status",
            401,
            "message",
            "Authentication required or token invalid"));
  }
}
