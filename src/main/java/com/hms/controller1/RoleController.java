package com.hms.controller;

import com.hms.dto.RoleDTO;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public String healthCheck() {
        return roleService.healthCheck();
    }

    @GetMapping("/getselectedrole")
    public ResponseEntity<RoleDTO> getSelectedRole(@RequestParam(name = "roleId") Long roleId) {
        if (roleId == null || roleId <= 0) return ResponseEntity.badRequest().build();
        try {
            RoleDTO result = roleService.getSelectedRole(roleId);
            return result == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to retrieve Role with ID {}", roleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getallroles")
    public ResponseEntity<Page<RoleDTO>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        if (page < 0 || size < 1 || size > 100) return ResponseEntity.badRequest().build();
        try {
            // Sort by the entity's primary-key property for consistent page ordering.
            return ResponseEntity.ok(roleService.getAllRoles(
                    PageRequest.of(page, size, Sort.by("roleId").ascending())));
        } catch (Exception e) {
            log.error("Failed to retrieve Role records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/addrole")
    public ResponseEntity<RoleDTO> addRole(@RequestBody RoleDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(roleService.addRole(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to add Role", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/updaterole")
    public ResponseEntity<RoleDTO> updateRole(
            @RequestParam(name = "roleId") Long roleId,
            @RequestBody RoleDTO dto) {
        if (roleId == null || roleId <= 0 || dto == null) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(roleService.updateRole(roleId, dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to update Role with ID {}", roleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleterole")
    public ResponseEntity<Void> deleteRole(@RequestParam(name = "roleId") Long roleId) {
        if (roleId == null || roleId <= 0) return ResponseEntity.badRequest().build();
        try {
            roleService.deleteRole(roleId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete Role with ID {}", roleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
