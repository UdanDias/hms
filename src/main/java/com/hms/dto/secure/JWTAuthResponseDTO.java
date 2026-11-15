package com.hms.dto.secure;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JWTAuthResponseDTO implements Serializable {
  private static final long serialVersionUID = 1L;
  private String token;
  private String tokenType;
  private long expiresIn;
  private Long userId;
  private String username;
  private String role;
}
