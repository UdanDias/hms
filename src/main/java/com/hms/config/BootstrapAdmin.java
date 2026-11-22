package com.hms.config;

import com.hms.dao.RoleDAO;
import com.hms.dao.UserDAO;
import com.hms.entities.RoleEntity;
import com.hms.entities.UserEntity;
import com.hms.util.ValidationUtil;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Explicit first-install action; disable and remove bootstrap environment values afterward. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true")
public class BootstrapAdmin implements ApplicationRunner {
  private final RoleDAO roles;
  private final UserDAO users;
  private final PasswordEncoder encoder;
  private final ValidationUtil validation;

  @Value("${BOOTSTRAP_ADMIN_USERNAME}")
  private String username;

  @Value("${BOOTSTRAP_ADMIN_EMAIL}")
  private String email;

  @Value("${BOOTSTRAP_ADMIN_PASSWORD}")
  private String password;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (!users.findAdministrators().isEmpty())
      throw new IllegalStateException("An administrator already exists; disable bootstrap");
    if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72)
      throw new IllegalArgumentException(
          "Bootstrap password must be 12+ characters and at most 72 UTF-8 bytes");
    if (!username.matches("[A-Za-z0-9_.-]{3,80}"))
      throw new IllegalArgumentException("Invalid bootstrap username");
    for (String type :
        List.of("ADMIN", "MANAGER", "RECEPTIONIST", "HOUSEKEEPING", "GUEST")) {
      if (roles.findByType(type).isEmpty()) {
        var role = new RoleEntity();
        role.setType(type);
        roles.save(role);
      }
    }
    var user = new UserEntity();
    user.setUsername(username);
    user.setEmail(email.trim().toLowerCase(Locale.ROOT));
    user.setPassword(encoder.encode(password));
    user.setRole(roles.findByType("ADMIN").orElseThrow());
    validation.validate(user);
    users.save(user);
    users.flush();
  }
}
