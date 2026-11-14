package com.hms.controller;

import com.hms.dto.RoleDTO;
import com.hms.service.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService service;

    @GetMapping
    public String healthCheck() {
        return "Role controller is running";
    }

    @GetMapping("/getselectedrole")
    public ResponseEntity<RoleDTO> getSelectedRole(@RequestParam("roleId") Long roleId) {
        return ResponseEntity.ok(service.getSelectedRole(roleId));
    }

    @GetMapping("/getallroles")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(service.getAllRoles());
    }

    @PostMapping("/addrole")
    public ResponseEntity<RoleDTO> addRole(@Valid @RequestBody RoleDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addRole(dto));
    }
}
