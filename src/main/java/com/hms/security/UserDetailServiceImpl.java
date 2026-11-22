package com.hms.security;

import com.hms.dao.UserDAO;
import com.hms.entities.UserEntity;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDetailServiceImpl implements UserDetailsService {
  private final UserDAO userDao;
  private static final Set<String> ROLES =
      Set.of("ADMIN", "MANAGER", "RECEPTIONIST", "HOUSEKEEPING", "GUEST");

  // Login accepts email. The resulting principal always exposes the canonical username.
  @Override
  public HmsUserDetails loadUserByUsername(String email) {
    return map(
        userDao
            .findByEmail(email.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials")));
  }

  public HmsUserDetails loadById(Long id) {
    return map(
        userDao.findById(id).orElseThrow(() -> new UsernameNotFoundException("Account not found")));
  }

  private HmsUserDetails map(UserEntity user) {
    if (user.getRole() == null || !ROLES.contains(user.getRole().getType()))
      throw new UsernameNotFoundException("Invalid account role");
    return new HmsUserDetails(
        user.getUserId(), user.getUsername(), user.getPassword(), user.getRole().getType());
  }
}
