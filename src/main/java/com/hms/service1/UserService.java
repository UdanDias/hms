package com.hms.service;

import com.hms.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
  UserDTO getSelectedUser(Long userId);
  Page<UserDTO> getAllUsers(Pageable pageable);

  UserDTO addUser(UserDTO dto);

  UserDTO updateUser(Long id, UserDTO dto);

  default String healthCheck() { return "User service is running"; }

  void deleteUser(Long id);
}
