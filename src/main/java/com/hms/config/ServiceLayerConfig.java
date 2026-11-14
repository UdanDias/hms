package com.hms.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ServiceLayerConfig {
  @Bean
  @ConditionalOnMissingBean(PasswordEncoder.class)
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}
