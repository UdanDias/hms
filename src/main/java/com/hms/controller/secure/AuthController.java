package com.hms.controller.secure;

import com.hms.dto.UserDTO;
import com.hms.dto.secure.*;
import com.hms.service.secure.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "app.security.auth-mode",
    havingValue = "local-jwt",
    matchIfMissing = true)
public class AuthController {
  private final AuthService authService;

  @PostMapping("/signin")
  public ResponseEntity<JWTAuthResponseDTO> signIn(@Valid @RequestBody SignInDTO dto) {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(authService.signIn(dto));
  }

  @PostMapping("/signup")
  public ResponseEntity<JWTAuthResponseDTO> signUp(@Valid @RequestBody SignUpDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .cacheControl(CacheControl.noStore())
        .body(authService.signUp(dto));
  }

  @GetMapping("/me")
  public ResponseEntity<UserDTO> me() {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(authService.getCurrentUser());
  }
}
