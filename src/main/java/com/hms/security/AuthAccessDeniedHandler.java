package com.hms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthAccessDeniedHandler implements AccessDeniedHandler {
  private final ObjectMapper mapper;

  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException error)
      throws IOException {
    response.setStatus(403);
    response.setContentType("application/json");
    mapper.writeValue(
        response.getOutputStream(),
        Map.of("timestamp", Instant.now().toString(), "status", 403, "message", "Access denied"));
  }
}
