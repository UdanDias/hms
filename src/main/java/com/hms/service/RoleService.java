package com.hms.service;

import com.hms.dto.RoleDTO;
import java.util.List;

public interface RoleService {
  RoleDTO getSelectedRole(Long roleId);

  List<RoleDTO> getAllRoles();

  RoleDTO addRole(RoleDTO dto);
}
