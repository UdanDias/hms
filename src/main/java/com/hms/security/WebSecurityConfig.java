package com.hms.security;

import com.hms.security.jwt.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@ConditionalOnProperty(
    name = "app.security.auth-mode",
    havingValue = "local-jwt",
    matchIfMissing = true)
public class WebSecurityConfig {
  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider(
      UserDetailServiceImpl users, PasswordEncoder encoder) {
    var provider = new DaoAuthenticationProvider(users);
    provider.setPasswordEncoder(encoder);
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
    return new ProviderManager(provider);
  }

  @Bean
  public FilterRegistrationBean<AuthFilter> authFilterRegistration(AuthFilter filter) {
    var registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      AuthFilter filter,
      AuthEntryPoint entry,
      AuthAccessDeniedHandler denied,
      @org.springframework.beans.factory.annotation.Qualifier("corsConfigurationSource")
          CorsConfigurationSource cors,
      DaoAuthenticationProvider provider)
      throws Exception {
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
                a.requestMatchers(HttpMethod.POST, "/api/v1/auth/signin", "/api/v1/auth/signup")
                    .permitAll()
                    .requestMatchers("/api/v1/web/public/**")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .authenticationProvider(provider)
        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
