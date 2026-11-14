package com.hms.service;

import com.hms.dto.RoleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleService {
  RoleDTO getSelectedRole(Long roleId);
  Page<RoleDTO> getAllRoles(Pageable pageable);

  RoleDTO addRole(RoleDTO dto);

  default String healthCheck() { return "Role service is running"; }

  RoleDTO updateRole(Long id, RoleDTO dto);

  void deleteRole(Long id);
}
