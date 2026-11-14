package com.hms.service;

import com.hms.dto.UserDTO;
import java.util.List;

public interface UserService {
  UserDTO getSelectedUser(Long userId);

  List<UserDTO> getAllUsers();

  UserDTO addUser(UserDTO dto);

  UserDTO updateUser(Long userId, UserDTO dto);
}
