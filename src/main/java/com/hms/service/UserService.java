package com.hms.service;

import com.hms.dto.UserDTO;
import java.util.List;

/** Contract only. Implement as a Spring @Service; perform queries, authorization and mapping there. */
public interface UserService {

    /** Return the DTO or throw ResourceNotFoundException when it does not exist. */
    UserDTO getSelectedUser(Long userId);

    /** Return a non-null list; use an empty list when no records are visible to the caller. */
    List<UserDTO> getAllUsers();
}
