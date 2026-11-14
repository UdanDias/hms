package com.hms.service.impl;

import com.hms.dao.*;
import com.hms.dto.UserDTO;
import com.hms.entities.*;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * HMS business service for User.
 * Maps entities at the service boundary; the DAO performs real SQL-level pagination.
 * All updates behave as partial updates (null DTO properties are left unchanged).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final PasswordEncoder passwordEncoder;

    private User getEntity(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid User ID");
        return userDao.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserDTO toDto(User entity) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setRoleId(entity.getRole() == null ? null : entity.getRole().getRoleId());
        dto.setPassword(null); // Never send password hashes to API consumers.
        return dto;
    }

    private void apply(UserDTO dto, User entity) {
        if (dto.getUsername() != null) entity.setUsername(dto.getUsername());
        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if (dto.getRoleId() != null) entity.setRole(
                    roleDao.findById(dto.getRoleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + dto.getRoleId())));
    }

    @Override
    public UserDTO getSelectedUser(Long id) {
        return toDto(getEntity(id));
    }

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        if (pageable == null || pageable.getPageSize() > 100) throw new IllegalArgumentException("Page size cannot exceed 100");
        return userDao.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public UserDTO addUser(UserDTO dto) {
        if (dto == null) throw new IllegalArgumentException("User DTO is required");
        if (dto.getPassword() == null || dto.getPassword().isBlank()) throw new IllegalArgumentException("Password is required");
        User entity = new User();
        apply(dto, entity);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity = userDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO dto) {
        if (dto == null) throw new IllegalArgumentException("User DTO is required");
        User entity = getEntity(id);
        // Password is re-encoded only if a new password is provided.
        apply(dto, entity);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity = userDao.saveAndFlush(entity);
        // Nothing extra.
        return toDto(entity);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User entity = getEntity(id);
        // Nothing extra.
        userDao.delete(entity);
        userDao.flush();
        // Nothing extra.
    }


}
