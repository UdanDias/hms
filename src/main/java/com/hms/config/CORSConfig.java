package com.hms.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;

@Configuration
public class CORSConfig {
  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins}") String origins) {
    var allowed =
        Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
    if (allowed.isEmpty()
        || allowed.stream()
            .anyMatch(
                s ->
                    s.contains("*")
                        || !(s.startsWith("https://") || s.startsWith("http://localhost:"))))
      throw new IllegalArgumentException(
          "Set exact HTTPS CORS origins (localhost HTTP allowed for development)");
    var cors = new CorsConfiguration();
    cors.setAllowedOrigins(allowed);
    cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Booking-Token"));
    cors.setAllowCredentials(false);
    cors.setMaxAge(3600L);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cors);
    return source;
  }
}
