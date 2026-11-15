package com.hms.service.impl.secure;

import com.hms.dao.*;
import com.hms.dto.UserDTO;
import com.hms.dto.secure.*;
import com.hms.entities.*;
import com.hms.exception.BusinessRuleException;
import com.hms.security.*;
import com.hms.security.jwt.JWTutils;
import com.hms.service.secure.AuthService;
import com.hms.util.EntityDTOConvert;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
@ConditionalOnProperty(
    name = "app.security.auth-mode",
    havingValue = "local-jwt",
    matchIfMissing = true)
public class AuthServiceImpl implements AuthService {
  private final UserDAO userDao;
  private final RoleDAO roleDao;
  private final CustomerDAO customerDao;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JWTutils jwtutils;
  private final ServiceSecurity security;
  private final EntityDTOConvert mapper;

  @Override
  public JWTAuthResponseDTO signIn(SignInDTO dto) {
    var auth =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
    return response((HmsUserDetails) auth.getPrincipal());
  }

  @Override
  public JWTAuthResponseDTO signUp(SignUpDTO dto) {
    if (dto.getPassword().getBytes(StandardCharsets.UTF_8).length > 72)
      throw new IllegalArgumentException("Password exceeds 72 UTF-8 bytes");
    String email = dto.getEmail().trim().toLowerCase(Locale.ROOT),
        username = dto.getUsername().trim();
    if (userDao.findByEmail(email).isPresent() || userDao.findByUsername(username).isPresent())
      throw new BusinessRuleException("Account cannot be registered with these details");
    var role =
        roleDao
            .findByType("GUEST")
            .orElseThrow(() -> new IllegalStateException("GUEST role must be provisioned"));
    var user = new UserEntity();
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setRole(role);
    userDao.save(user);
    var customer = new CustomerEntity();
    customer.setUser(user);
    customer.setName(dto.getName().trim());
    customer.setPhone(dto.getPhone().trim());
    customer.setEmail(email);
    customerDao.save(customer);
    userDao.flush();
    return response(
        new HmsUserDetails(
            user.getUserId(), user.getUsername(), user.getPassword(), role.getType()));
  }

  @Override
  @Transactional(readOnly = true)
  public UserDTO getCurrentUser() {
    return mapper.convertUserEntityToUserDTO(security.getCurrentUser());
  }

  private JWTAuthResponseDTO response(HmsUserDetails user) {
    return new JWTAuthResponseDTO(
        jwtutils.generateToken(user),
        "Bearer",
        jwtutils.getExpiresIn(),
        user.userId(),
        user.getUsername(),
        user.role());
  }
}
