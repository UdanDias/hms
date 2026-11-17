package com.hms.security;

import com.hms.security.jwt.AuthEntryPoint;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@ConditionalOnProperty(name = "app.security.auth-mode", havingValue = "oauth2")
public class OAuth2SecurityConfig {
  @Bean
  public JwtDecoder jwtDecoder(
      @Value("${app.security.oauth2.issuer}") String issuer,
      @Value("${app.security.oauth2.audience}") String audience) {
    NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
    OAuth2TokenValidator<Jwt> audienceValidator = new ApiAudienceValidator(audience);
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(issuer), audienceValidator));
    return decoder;
  }

  @Bean
  public SecurityFilterChain oauth2FilterChain(
      HttpSecurity http,
      JwtDecoder decoder,
      UserDetailServiceImpl users,
      AuthEntryPoint entry,
      AuthAccessDeniedHandler denied,
      @org.springframework.beans.factory.annotation.Qualifier("corsConfigurationSource")
          CorsConfigurationSource cors,
      @Value("${app.security.oauth2.subject-bindings}") String bindings)
      throws Exception {
    Map<String, Long> accounts = new HashMap<>();
    for (String pair : bindings.split(",")) {
      String[] parts = pair.trim().split("=", 2);
      if (parts.length != 2 || parts[0].isBlank())
        throw new IllegalArgumentException("Expected subject=localUserId bindings");
      Long id = Long.valueOf(parts[1]);
      if (id <= 0 || accounts.putIfAbsent(parts[0], id) != null)
        throw new IllegalArgumentException("Invalid or duplicate subject binding");
    }
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(c -> c.configurationSource(cors))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .requestCache(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(e -> e.authenticationEntryPoint(entry).accessDeniedHandler(denied))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/api/v1/auth/**")
                    .denyAll()
                    .requestMatchers("/api/v1/web/public/**")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            o ->
                o.authenticationEntryPoint(entry)
                    .accessDeniedHandler(denied)
                    .jwt(
                        j ->
                            j.decoder(decoder)
                                .jwtAuthenticationConverter(
                                    jwt -> {
                                      try {
                                        Long id = accounts.get(jwt.getSubject());
                                        if (id == null)
                                          throw new IllegalArgumentException("Unlinked subject");
                                        var user = users.loadById(id);
                                        return new UsernamePasswordAuthenticationToken(
                                            user, null, user.getAuthorities());
                                      } catch (org.springframework.security.core
                                              .AuthenticationException
                                          | IllegalArgumentException ex) {
                                        throw new OAuth2AuthenticationException(
                                            new OAuth2Error("invalid_token"), "Unlinked account");
                                      }
                                    })));
    return http.build();
  }
}
