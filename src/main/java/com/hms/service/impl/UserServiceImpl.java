package com.hms.service.impl;

import com.hms.dao.HmsDao;
import com.hms.dto.*;
import com.hms.entities.*;
import com.hms.service.*;
import com.hms.service.impl.support.*;
import com.hms.util.EntityDTOConvert;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
  private final HmsDao dao;
  private final EntityDTOConvert mapper;
  private final ServiceSupport s;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserDTO getSelectedUser(Long userId) {
    s.actor();
    var e = dao.require(UserEntity.class, userId);
    s.checkRead(e);
    return mapper.toDTO(e);
  }

  @Override
  public List<UserDTO> getAllUsers() {
    s.actor();
    return dao.all(UserEntity.class).stream().filter(s::visible).map(mapper::toDTO).toList();
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public UserDTO addUser(UserDTO dto) {
    s.admin();
    var e = mapper.toEntity(dto);
    prepare(e, null);
    s.validate(e);
    dao.persist(e);
    dao.flush();
    return finish(e, "CREATE");
  }

  @Override
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public UserDTO updateUser(Long id, UserDTO dto) {
    s.admin();
    var old = dao.require(UserEntity.class, id);
    var e = mapper.toEntity(dto);
    prepare(e, old);
    s.validate(e);
    BeanUtils.copyProperties(e, old, "userId", "createdAt", "updatedAt");
    return finish(old, "UPDATE");
  }

  private void prepare(UserEntity e, UserEntity old) {
    s.text(e.getUsername(), "Username");
    s.text(e.getEmail(), "Email");
    s.require(e.getRole() != null, "Role is required");
    e.setUsername(e.getUsername().trim());
    e.setEmail(e.getEmail().trim().toLowerCase(Locale.ROOT));
    if (old != null) {
      s.require(
          old.getUsername().equals(e.getUsername()),
          "Username is immutable because it links authentication to the local account");
      if ("ADMIN".equals(old.getRole().getType()) && !"ADMIN".equals(e.getRole().getType()))
        s.require(
            dao.query(UserEntity.class, "e.role.type = 'ADMIN'").size() > 1,
            "Cannot demote the last administrator");
    }
    String raw = e.getPassword();
    if (raw == null || raw.isBlank()) {
      s.require(old != null, "Password is required");
      e.setPassword(old.getPassword());
    } else {
      s.require(
          raw.length() >= 12 && raw.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72,
          "Password must be at least 12 characters and at most 72 UTF-8 bytes");
      e.setPassword(passwordEncoder.encode(raw));
    }
  }

  private UserDTO finish(UserEntity e, String action) {
    s.validate(e);
    s.audit(action, "User", e.getUserId());
    dao.flush();
    return mapper.toDTO(e);
  }
}
