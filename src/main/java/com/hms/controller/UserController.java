package com.hms.controller;

import com.hms.dto.UserDTO;
import com.hms.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    public String healthCheck() {
        return "User controller is running";
    }

    @GetMapping("/getselecteduser")
    public ResponseEntity<UserDTO> getSelectedUser(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(service.getSelectedUser(userId));
    }

    @GetMapping("/getallusers")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @PostMapping("/adduser")
    public ResponseEntity<UserDTO> addUser(@Valid @RequestBody UserDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addUser(dto));
    }

    @PutMapping("/updateuser")
    public ResponseEntity<UserDTO> updateUser(@RequestParam("userId") Long userId, @Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(service.updateUser(userId, dto));
    }
}
