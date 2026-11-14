package com.hms.service.impl;

import com.hms.dao.HotelDAO;
import com.hms.dao.RoleDAO;
import com.hms.dao.UserDAO;
import com.hms.dto.UserDTO;
import com.hms.entities.RoleEntity;
import com.hms.entities.UserEntity;
import com.hms.exception.RoleNotFoundException;
import com.hms.exception.UserNotFoundException;
import com.hms.security.ServiceSecurity;
import com.hms.service.UserService;
import com.hms.util.AuditRecorder;
import com.hms.util.EntityDTOConvert;
import com.hms.util.ValidationUtil;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class UserServiceImpl implements UserService {
  private final RoleDAO roleDao;
  private final UserDAO userDao;
  private final HotelDAO hotelDao;
  private final EntityDTOConvert entityDTOConvert;
  private final ServiceSecurity serviceSecurity;
  private final ValidationUtil validationUtil;
  private final AuditRecorder auditRecorder;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional(readOnly = true)
  public UserDTO getSelectedUser(Long userId) {
    serviceSecurity.getCurrentUser();
    UserEntity userEntity =
        userDao.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    serviceSecurity.checkRead(userEntity);
    return entityDTOConvert.convertUserEntityToUserDTO(userEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserDTO> getAllUsers() {
    serviceSecurity.getCurrentUser();
    List<UserEntity> entities =
        userDao.findAll().stream().filter(serviceSecurity::canRead).toList();
    return entityDTOConvert.convertUserEntityListToUserDTOList(entities);
  }

  @Override
  public UserDTO addUser(UserDTO userDTO) {
    serviceSecurity.requireRoles("ADMIN");
    hotelDao.lockAllForWrite();
    UserEntity userEntity = entityDTOConvert.convertUserDTOToUserEntity(userDTO);
    if (userDTO.getRoleId() != null) {
      RoleEntity role =
          roleDao
              .findById(userDTO.getRoleId())
              .orElseThrow(() -> new RoleNotFoundException("Role not found"));
      userEntity.setRole(role);
    }

    validateUserForSave(userEntity, null);
    validationUtil.validate(userEntity);
    userDao.save(userEntity);
    auditRecorder.recordAction("CREATE", "User", userEntity.getUserId());
    userDao.flush();
    return entityDTOConvert.convertUserEntityToUserDTO(userEntity);
  }

  @Override
  public UserDTO updateUser(Long userId, UserDTO userDTO) {
    serviceSecurity.requireRoles("ADMIN");
    hotelDao.lockAllForWrite();
    UserEntity userEntity =
        userDao.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    UserEntity requestedUser = entityDTOConvert.convertUserDTOToUserEntity(userDTO);
    if (userDTO.getRoleId() != null) {
      RoleEntity role =
          roleDao
              .findById(userDTO.getRoleId())
              .orElseThrow(() -> new RoleNotFoundException("Role not found"));
      requestedUser.setRole(role);
    }

    validateUserForSave(requestedUser, userEntity);
    validationUtil.validate(requestedUser);
    userEntity.setUsername(requestedUser.getUsername());
    userEntity.setEmail(requestedUser.getEmail());
    userEntity.setPassword(requestedUser.getPassword());
    userEntity.setRole(requestedUser.getRole());
    validationUtil.validate(userEntity);
    userDao.save(userEntity);
    auditRecorder.recordAction("UPDATE", "User", userEntity.getUserId());
    userDao.flush();
    return entityDTOConvert.convertUserEntityToUserDTO(userEntity);
  }

  private void validateUserForSave(UserEntity userEntity, UserEntity existingUser) {
    validationUtil.text(userEntity.getUsername(), "Username");
    validationUtil.text(userEntity.getEmail(), "Email");
    validationUtil.require(userEntity.getRole() != null, "Role is required");
    userEntity.setUsername(userEntity.getUsername().trim());
    userEntity.setEmail(userEntity.getEmail().trim().toLowerCase(Locale.ROOT));
    if (existingUser != null) {
      validationUtil.require(
          existingUser.getUsername().equals(userEntity.getUsername()),
          "Username is immutable because it links authentication to the local account");
      if ("ADMIN".equals(existingUser.getRole().getType())
          && !"ADMIN".equals(userEntity.getRole().getType()))
        validationUtil.require(
            userDao.findAdministrators().size() > 1, "Cannot demote the last administrator");
    }
    String raw = userEntity.getPassword();
    if (raw == null || raw.isBlank()) {
      validationUtil.require(existingUser != null, "Password is required");
      userEntity.setPassword(existingUser.getPassword());
    } else {
      validationUtil.require(
          raw.length() >= 12 && raw.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72,
          "Password must be at least 12 characters and at most 72 UTF-8 bytes");
      userEntity.setPassword(passwordEncoder.encode(raw));
    }
  }
}
