package com.hms.controller;

import com.hms.dto.RoleDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public String healthCheck() {
        return "Role controller is running";
    }

    @GetMapping("/getselectedrole")
    public ResponseEntity<RoleDTO> getSelectedRole(
            @RequestParam(name = "roleId") Long roleId) {
        if (roleId == null || roleId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            RoleDTO result = roleService.getSelectedRole(roleId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Role with ID {}", roleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallroles")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        try {
            List<RoleDTO> results = roleService.getAllRoles();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to retrieve Roles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
