package com.hms.service;

import com.hms.dto.RoleDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface RoleService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    RoleDTO getSelectedRole(Long roleId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<RoleDTO> getAllRoles();
}
